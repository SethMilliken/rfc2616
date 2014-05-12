Bare Bones RFC2616 (HTTP/1.1) Server Implementation
================================================================================

Getting Started
--------------------------------------------------------------------------------
- get sbt
- enter sbt `sbt`
- `test` to run test suite
- `run` to start server

Design
--------------------------------------------------------------------------------
This minimal RFC2616 server accepts connections on port 8888, and responds to HTTP GET and HEAD requests. The server infrastructure is implemented as an Akka IO actor system to handle the underlying transport mechanics asynchronously and without needing to deal explicitly with concurrency concerns.

- HttpServer starts
  - HttpServer creates an HttpManager actor to bind to a TCP socket via the Akka IO manager actor and listen for incoming connections
  - Connections are dispatched by the IO manager to HttpHandler actors
    - HttpHandler actor creates an HttpRequest object from the raw incoming data
      - HttpRequest object parses the data
      - HttpRequest object generates an appropriate HttpResponse
    - HttpHandler actor writes HttpResponse data to IO connection
    - HttpHandler actor tells IO manager to close connection
      - HttpHandler actor is torn down
- HttpServer stops
  - Actor system tears down all actors and frees resources
    
TODO
--------------------------------------------------------------------------------
- add tests for responses
- add ScalaDoc comments
- divide classes into separate, appropriate files
- arrange files into appropriate filesystem layout
- add error handling
- handle routing
- handle file serving
- add timeouts
- clean up response generation
- make HttpRequest.lines private
- clean up README, separate documentation from setup
- create commandline tool to run server
- create standalone configuration file
- consider using Iteratees to parse request

NOTABLE MISSING FEATURES
--------------------------------------------------------------------------------
- limited methods supported (only GET and HEAD)
- no caching
- no content codings
- no transfer codings
- no multipart
- no ranges
- no cookies
- no authorization
- no client error detection/reporting
- no significant security considerations
- no accommodations for prior protocol versions
- no support for multi-line header values
