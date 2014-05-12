Description
================================================================================
As a Platform Engineer at Urban Airship, you'll be building and maintaining distributed systems that support billions of requests per day. We provide RESTful APIs for our backend tiers as well as backend services that must be highly available and scalable. Platform Engineering utilizes many technologies including Kafka, Zookeeper, HBase, Cassandra, Google protobuf, Jetty, Guava, etc. The systems that you will be designing/maintaining will follow the concept of service-oriented architecture. To demonstrate your understanding of this concept, choose a well known application layer RFC, and implement a minimal service that can respond to clients. We write a lot of Java and Python, but you may write this project in any language. Example RFCs include DNS (http://www.ietf.org/rfc/rfc1035.txt) or TFTP (https://www.ietf.org/rfc/rfc1350.txt).

The goal of this project is to provide us with an example of your coding style and demonstrate your ability to interpret a well-known specification. We intend this project to take no longer than 3 hours, so choose a subset of features that you would like to implement.

Questions
================================================================================

Along with your submission, please answer the following:

What features did you choose to implement and why?
--------------------------------------------------------------------------------

- I implemented simple versions of the core domain objects described in the RFC (HttpRequest, HttpResponse) along with assembling a basic connection management infrastructure. This provides just enough functionality to provide a service that can respond to clients in a way compliant with the protocol described in the RFC. Most of the unimplemented features are refinements of the basic protocol, and may not be essential to a specific use case for the server. This implementation provides a skeleton on which unimplemented features could be built as needed.

If you had to do this project again, what would you do differently and why?
--------------------------------------------------------------------------------

- Given that most of the refinements of HTTP/1.1 over HTTP/1.0 are features that I did not implement for this minimal server, I would probably choose to work from the simpler HTTP/1.0 RFC (RFC 1945).

- I would attempt from the outset to better capture all requirements as tests in order to help myself stay focused on getting all core functionality implemented prior to experimenting with alternative approaches to already working code.

- I might also choose a different implementation language in order to compare and contrast the approach I took with Scala.
