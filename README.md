# A simple HTTP server

Please build with maven.

There are both Java JUnit and python scripts for testing.

To build with tests and start the server execute the following on the command line in the project root directory `httpd`:

```
mvn package
cd target
java -jar httpd-0.0.1-SNAPSHOT.jar
```

The python test scripts are under `src/test/python` and require that the server (httpd.Server) be running on localhost and listening on port 80.

To run the python tests execute the following in another window from the project root directory `httpd`:

```
cd src/test/python
python keep-alive.py
python etag-test.py
```

## Design considerations:

TODO Response represented as a class parallel to Request class.
