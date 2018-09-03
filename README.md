# A simple HTTP server

Should be built with maven.

There are both Java JUnit and python scripts for testing. The python scripts are under `src/test/python` and require that the server (httpd.Server) be running on localhost and listening on port 80.

To build and run with tests execute the following on the command line in the root project directory `httpd`:

```
mvn clean install
cd target
java -jar httpd-0.0.1-SNAPSHOT.jar
```

To run the python tests execute the following in another window from the root directory `httpd`:

```
cd src/test/python
python keep-alive.py
python etag-test.py
```
