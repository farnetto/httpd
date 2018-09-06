# A simple HTTP server

Please build with maven.

There are both Java JUnit and python scripts for testing.

To build with tests and start the server execute the following on the command line in the project root directory `httpd`:

```
mvn package
cd target
java -jar httpd-0.0.1-SNAPSHOT.jar
```

The python scripts are under `src/test/python` and require that the server (httpd.Server) be running on localhost and listening on port 80.

To run the python tests execute the following in another window from the project root directory `httpd`:

```
cd src/test/python
python keep-alive.py
python etag-test.py
```
## Design Considerations

Important Abstractions are implemented as separate classes: `Request`, `Header`, `StatusCode`, etc. `Response` class TBD

Multithreaded: the `Server` class launches a new thread per connection. To allow for greater scaling and to protect against overload a thread pool could be built in future.

Using the class `HttpError` an http status code can be returned on an error condition at any point in the code by throwing an exception, keeping error handling separate from normal application logic.
 
The abstract `HttpdTest` with the mocked `Socket` and piped streams allows automated unit-testing requests and reponse types without starting the server or any changes to application code.



