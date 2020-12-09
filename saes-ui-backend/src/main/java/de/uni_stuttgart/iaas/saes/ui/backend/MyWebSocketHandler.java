/*
* This file is part of the Serverless Application Extraction System (SAES)
*
* The Serverless Application Extraction System is licensed under under
* the Apache License, Version 2.0. Please see the included COPYING file
* for license information.
*/
package de.uni_stuttgart.iaas.saes.ui.backend;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/** Spring web socket handler */
public class MyWebSocketHandler extends TextWebSocketHandler {
	private static final Logger LOG = LoggerFactory.getLogger(MyWebSocketHandler.class); 
	{
		LOG.info("Created.");
	}

	@SuppressWarnings("serial")
	private Map<String, WeakReference<WebSocketThread>> threads = new HashMap<>() {
		public WeakReference<WebSocketThread> get(Object key) {
			/* get() with additional cleanup */
			var wref = super.get(key);
			if (wref.get() == null) {
				super.remove(key);
				return null;
			}
			return wref;
		}
	};

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		super.afterConnectionEstablished(session);
		
		WebSocketThread t = new WebSocketThread(session);
		t.start();
		threads.put(session.getId(), new WeakReference<>(t));
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		super.handleTextMessage(session, message);

		WebSocketThread t;
		var ref = threads.get(session.getId());
		if (ref == null || (t = ref.get()) == null) {
			return;
		}
		t.incomingMessages.offer(message);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		super.afterConnectionClosed(session, status);

		WebSocketThread t;
		var ref = threads.get(session.getId());
		if (ref == null || (t = ref.get()) == null) {
			return;
		}
		t.interrupt();
		threads.remove(session.getId());
	}
}
