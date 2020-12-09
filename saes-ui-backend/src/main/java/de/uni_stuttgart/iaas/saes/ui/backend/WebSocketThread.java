/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.ui.backend;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import de.uni_stuttgart.iaas.saes.code_analysis.plugin.java.DeploymentModelPluginSam;
import de.uni_stuttgart.iaas.saes.common.db.KnowledgeBaseDAO;
import de.uni_stuttgart.iaas.saes.common.util.SaesException;
import de.uni_stuttgart.iaas.saes.deployment_model.DeploymentModels;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Mapping;
import de.uni_stuttgart.iaas.saes.knowledge_base.domain.Taxonomy;
import de.uni_stuttgart.iaas.saes.mapper.Mapper;
import de.uni_stuttgart.iaas.saes.mapper.plugin.aws.AwsSamImplicitConnectionsPlugin;

/** User interaction thread: Handles "conversation" with the user */
public class WebSocketThread extends Thread {
	private static final Logger LOG = LoggerFactory.getLogger(WebSocketThread.class);

	private static final String KNOWLEDGE_BASE_DBNAME = "kb";

	private static final String START_NEW_SESSION = "Start new session";
	private static final String LOAD_OLD_RESULTS = "Load old analysis results";
	private static final String DOWNLOAD_KB = "Download current stored knowledge base";
	private static final String UPLOAD_KB = "Upload new knowledge base data";

	/* package-private */ final BlockingQueue<TextMessage> incomingMessages = new LinkedBlockingQueue<TextMessage>();
	private final WebSocketSession session;
	private final Gson gson = new Gson();

	/** @param session the WebSocket to talk to */
	public WebSocketThread(WebSocketSession session) {
		this.session = session;
	}

	@Override
	public void run() {
		try {
			try {
				if (!"CLIENT_HELLO".equals(exchangeMessage("SERVER_HELLO", 10))) {
					throw new SaesException("Did not receive a Client hello");
				}
				statusMessage("Initializing");
				ArangoDB arangoDB;
				try {
					arangoDB = new ArangoDB.Builder().build();
					if (!arangoDB.db().exists()) {
						throw new ArangoDBException("System database does not exist");
					}
				} catch (ArangoDBException e) {
					throw new SaesException("Did you start ArangoDB? Could not connect: " + e.getMessage(), e);
				}

				if (!arangoDB.db(KNOWLEDGE_BASE_DBNAME).exists()) {
					byte[] upload = binaryUploadQuestion(
							"No knowledge base data exists yet. Please upload a knowledge base zip file.");
					statusMessage("processing");
					processDbUpload(upload, arangoDB);
					doneMessage("New knowledge base installed.");
					return;
				}

				var mainMenuSelection = singleChoiceQuestion("What would you like to do?", START_NEW_SESSION,
						UPLOAD_KB);
				if (START_NEW_SESSION.equals(mainMenuSelection)) {
					String name = null;
					String errorMessage = null;
					while (name == null) {
						var proposedName = textboxQuestion(errorMessage != null
								? "Sorry, " + errorMessage + "\nPlease enter a name for the analysis."
								: "Please enter a name for the analysis.");
						if (proposedName.length() < 3) {
							errorMessage = "the analysis name must have at least 3 letters.";
							continue;
						}
						if (proposedName.length() > 20) {
							errorMessage = "the analysis name must have at most 20 letters.";
							continue;
						}
						if (!proposedName.matches("^[a-zA-Z0-9_-]*$")) {
							errorMessage = "only letters, numbers, dashes and underscores are allowed.";
							continue;
						}
						name = proposedName;
					}
					String deplModel = fileUploadQuestion("Please select the deployment model");

					statusMessage("Running extraction");
					Properties properties = new Properties();
					String dbName = "analysis-" + dateStamp() + "-" + name;
					if (!arangoDB.db(dbName).exists()) {
						arangoDB.db(dbName).create();
					}
					properties.setProperty("extraction-db-name", dbName);
					properties.setProperty("knowledge-base-db-name", KNOWLEDGE_BASE_DBNAME);
					properties.setProperty("model-db-name", dbName);
					properties.setProperty("sam-model", deplModel);

					DeploymentModels runner = new DeploymentModels(arangoDB, properties);
					runner.registerPlugin(new DeploymentModelPluginSam());
					runner.run();

					statusMessage("Mapping...");
					Mapper mapper = new Mapper(arangoDB, properties);
					mapper.registerPlugin(new AwsSamImplicitConnectionsPlugin());
					mapper.run();
					List<JsonElement> edges = mapper.implicitConnections();

					statusMessage("Mapped. Preparing output...");
					List<JsonElement> vertices = new ArrayList<>();
					arangoDB.db(dbName).query("FOR t in @@col RETURN t", Map.of("@col", "elements"), BaseDocument.class)
							.map(d -> {
								var m = new HashMap<>(d.getProperties());
								m.put("_id", d.getId());
								return gson.toJsonTree(m);
							}).forEach(vertices::add);

					showGraph(vertices, edges);

				} else if (UPLOAD_KB.equals(mainMenuSelection)) {
					byte[] upload = binaryUploadQuestion("Upload a knowledge base zip file here.");
					statusMessage("processing");
					processDbUpload(upload, arangoDB);
					doneMessage("New knowledge base installed.");
				} else {
					errorMessage("Not yet implemented");
				}
			} catch (IOException e) {
				e.printStackTrace();
				session.close();
			} catch (SaesException e) {
				e.printStackTrace();
				errorMessage(e.getMessage());
				session.close();
			} catch (JsonSyntaxException e) {
				e.printStackTrace();
				errorMessage("Invalid JSON data sent by webclient");
			} catch (InterruptedException e) {
				session.close();
				return;
			}
		} catch (IOException e) {
			// thrown while handling exceptions, ignore.
		}

	}

	private void processDbUpload(byte[] upload, ArangoDB arangoDB) throws IOException {
		var bais = new ByteArrayInputStream(upload);
		var zis = new ZipInputStream(bais, StandardCharsets.UTF_8);
		if (arangoDB.db(KNOWLEDGE_BASE_DBNAME).exists()) {
			arangoDB.db(KNOWLEDGE_BASE_DBNAME).drop();
		}
		arangoDB.db(KNOWLEDGE_BASE_DBNAME).create();
		var mappingDAO = new KnowledgeBaseDAO<Mapping>(arangoDB.db(KNOWLEDGE_BASE_DBNAME), Mapping.class);
		var taxonomyDAO = new KnowledgeBaseDAO<Taxonomy>(arangoDB.db(KNOWLEDGE_BASE_DBNAME), Taxonomy.class);
		ZipEntry entry;
		while ((entry = zis.getNextEntry()) != null) {
			var baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int len;
			while ((len = zis.read(buffer)) != -1) {
				baos.write(buffer, 0, len);
			}
			String entryStr = baos.toString(StandardCharsets.UTF_8);
			Map<String, Object> entryBasedoc = gson.<Map<String, Object>>fromJson(entryStr, Map.class);
			if (entry.getName().startsWith("mapping")) {
				var mapping = Mapping.fromBaseDoc(entryBasedoc);
				mappingDAO.put(entry.getName(), mapping);
			} else if (entry.getName().startsWith("taxonom")) {
				var taxonomy = Taxonomy.fromBaseDoc(entryBasedoc);
				taxonomyDAO.put(entry.getName(), taxonomy);
			}
		}
	}

	private String singleChoiceQuestion(String question, String... values) throws IOException, InterruptedException {
		var reply = exchangeMessage("SINGLE_CHOICE " + gson.toJson(//
				Map.of(//
						"message", question, //
						"choices", List.of(values)//
				)//
		));
		@SuppressWarnings("unchecked")
		Map<String, Object> replyJson = gson.fromJson(reply, Map.class);
		return String.valueOf(replyJson.get("reply"));
	}

	private String textboxQuestion(String question) throws IOException, InterruptedException {
		var reply = exchangeMessage("TEXTBOX " + gson.toJson(//
				Map.of(//
						"message", question //
				)//
		));
		@SuppressWarnings("unchecked")
		Map<String, Object> replyJson = gson.fromJson(reply, Map.class);
		return String.valueOf(replyJson.get("reply"));
	}

	private String fileUploadQuestion(String question) throws IOException, InterruptedException {
		var reply = exchangeMessage("FILE_UPLOAD " + gson.toJson(//
				Map.of(//
						"message", question //
				)//
		));
		@SuppressWarnings("unchecked")
		Map<String, Object> replyJson = gson.fromJson(reply, Map.class);
		return String.valueOf(replyJson.get("reply"));
	}

	private byte[] binaryUploadQuestion(String question) throws IOException, InterruptedException {
		var reply = exchangeMessage("FILE_UPLOAD_BINARY " + gson.toJson(//
				Map.of(//
						"message", question //
				)//
		));
		@SuppressWarnings("unchecked")
		Map<String, Object> replyJson = gson.fromJson(reply, Map.class);
		return Base64.getDecoder().decode(String.valueOf(replyJson.get("reply")));
	}

	private void binaryDownloadOffer(String message, byte[] file) throws IOException, InterruptedException {
		sendMessage("FILE_DOWNLOAD_BINARY " + gson.toJson(//
				Map.of(//
						"message", message, //
						"file", Base64.getEncoder().encodeToString(file))//
		));
	}

	private void errorMessage(String message) throws IOException {
		sendMessage("ERROR " + gson.toJson(//
				Map.of(//
						"message", message //
				)//
		));
	}

	private void doneMessage(String message) throws IOException {
		sendMessage("DONE " + gson.toJson(//
				Map.of(//
						"message", message //
				)//
		));
	}

	private void statusMessage(String message) throws IOException {
		sendMessage("STATUS " + gson.toJson(//
				Map.of(//
						"message", message //
				)//
		));
	}

	private void showGraph(List<JsonElement> vertices, List<JsonElement> edges) throws IOException {
		var jsonVertices = new JsonArray();
		vertices.forEach(jsonVertices::add);
		var jsonEdges = new JsonArray();
		edges.forEach(jsonEdges::add);

		var json = new JsonObject();
		json.add("vertices", jsonVertices);
		json.add("edges", jsonEdges);
		sendMessage("SHOW_GRAPH " + gson.toJson(json));
	}

	private String exchangeMessage(String question) throws IOException, InterruptedException {
		sendMessage(question);
		return receiveMessage().getPayload();
	}

	private String exchangeMessage(String question, int timeoutSeconds) throws IOException, InterruptedException {
		sendMessage(question);
		return receiveMessage(timeoutSeconds).getPayload();
	}

	private void sendMessage(String value) throws IOException {
		session.sendMessage(new TextMessage(value));
	}

	private TextMessage receiveMessage() throws InterruptedException {
		return receiveMessage(3600);
	}

	private TextMessage receiveMessage(int timeoutSeconds) throws InterruptedException {
		var msg = incomingMessages.poll(timeoutSeconds, TimeUnit.SECONDS);
		if (msg == null) {
			throw new SaesException("Timed out waiting for client response");
		}
		return msg;
	}

	private String dateStamp() {
		return new SimpleDateFormat("yyMMdd-HHmmss").format(new Date());
	}
}
