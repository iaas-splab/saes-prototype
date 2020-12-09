Serverless Application Extraction System prototype
==================================================

Prerequisites
-------------

* Java 11
* ArangoDB running on localhost

Developing
----------

* Make sure to set up [Project Lombok support](https://projectlombok.org/setup/overview) in your IDE
* Import the main maven project (pom.xml in the root directory)
* Your IDE should automatically import subprojects or prompt you to

Building
--------

Prerequisite: A Maven installation that knows where to find the Java 11 JDK

To build just the application:
```
$ mvn verify
```

To also build the combined javadoc:
```
$ mvn verify javadoc:javadoc javadoc:aggregate
```

During a maven build, tests will be run. If an ArangoDB is running on localhost, additional tests will be run that test the database interaction classes. To skip this, use:
```
$ mvn verify -DskipTests=true
```

Running
-------

To run the application, use:

```
$ mvn verify # skip if you've already built the application
$ cd saes-ui-backend
$ cd target
$ java -jar saes-ui-backend-1.0.0.jar
```

Then, open http://127.0.0.1:8080/ in your browser.

For an example knowledge base, you can use the included file `saes-example-kb/example-kb-minimal.zip`.

For an example model, [this file](https://github.com/awsdocs/aws-lambda-developer-guide/blob/main/sample-apps/s3-java/template.yml) is recommended.

Documentation
-------------

You can find the Javadoc documentation at:
https://iaas-splab.github.io/saes-prototype/


License
-------

```
This file is part of the Serverless Application Extraction System (SAES)

The Serverless Application Extraction System is licensed under under
the Apache License, Version 2.0. Please see the included COPYING file
for license information.
```
