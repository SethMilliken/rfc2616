package net.araxia.http;

import akka.actor.{ ActorSystem, Actor, Props }
import akka.event.Logging
import akka.io.{ IO, Tcp }
import akka.util.{ ByteString }
import java.net.InetSocketAddress

// Configuration
object HttpServerConfig {
    val PORT = 8888
    val HOST = "localhost"
    val SERVER_HEADER = "Scala-RFC2616-Minimal"
}

// Request Handling Actor System
case object Start

object HttpServer extends App {
    override def main(args: Array[String]) = {
        val system = ActorSystem("HttpServer")
        val manager = system.actorOf(Props[HttpManager], "manager")
        manager ! Start
    }
}

class HttpManager extends Actor {
    import Tcp._
    import HttpServerConfig._
    import context.system

    val log = Logging(context.system, this)

    def receive = {
        case Start =>
            log.info("Starting connection manager.")
            IO(Tcp) ! Bind(self, new InetSocketAddress(HOST, PORT))
        case bound @ Bound(localAddress) =>
            log.info(s"Bound to ${localAddress}")
        case CommandFailed(binding: Bind) =>
            log.info(s"Could not bind to ${binding.localAddress}")
            context stop self
        case connnected @ Connected(remote, local) =>
            val handler = context.actorOf(Props[HttpHandler])
            sender() ! Register(handler)
    }
}

class HttpHandler extends Actor {
    import Tcp._

    val log = Logging(context.system, this)

    def receive = {
        case Received(data) => handleRequest(new HttpRequest(data))
        case PeerClosed => context stop self
    }

    def handleRequest(request: HttpRequest) = {
        log.info("handling incoming request")
        val response = request.response
        sender() ! Write(response.result)
        sender() ! Close
        log.info("handled request")
    }

}

// Parsing Helpers
object ParseTokens {
    val SP = " "
    val CRLF= "\r\n"
    val EOL = """(\r\n|\n)"""
    val EMPTY_LINE = """^(\r\n|\n)$"""
    val EmptyLine = EMPTY_LINE.r
    // Valid Message Header (4.2)
    val ValidHeaderLine = """([^:\s]*):\s+(.*)""".r
    // Valid Request Line (5.1)
    val ValidRequestLine = """(.*)\s+(.*)\s+(.*)""".r
}

// Domain Model Objects

// Http Message (4)
abstract class HttpMessage {
    import ParseTokens._

    type Headers = Array[Header]
    type MessageBody = Array[String]

    case class Header(name: String, value: String) {
        override def toString = s"${name}: ${value}"
    }
    object Header {
        def apply(line: String) = line match {
            case ValidHeaderLine(name, value) => new Header(name, value)
        }
    }

}

// Request Message (5)
class HttpRequest(val data: ByteString) extends HttpMessage {
    import annotation.tailrec
    import ParseTokens._

    private[this] var requestLine: RequestLine = _
    private[this] var rawHeaders: Headers = _
    private[this] var rawBody: MessageBody = _
    private[this] var rawResponse: Option[HttpResponse] = None
    var allLines: Option[Array[String]] = None

    def method = requestLine.method
    def requestUri = requestLine.requestUri
    def protocolVersion = requestLine.protocolVersion
    def body = rawBody
    def headers = rawHeaders
    def response = {
        if (rawResponse == None) respond
        rawResponse.get
    }
    def lines = {
        if (allLines == None) Array()
        allLines.get
    }

    private case class RequestLine(method: String, requestUri: String, protocolVersion: String)
    private object RequestLine {
        def apply(line: String) = line match {
            case ValidRequestLine(method, uri, version) => new RequestLine(method, uri, version)
        }
     }

     private def headersFromRawLines(rawLines: Array[String]): Array[Header] = {
         @tailrec def extractHeaders(lines: Array[String], acc: Array[Header]): Array[Header] = lines match {
             case Array(ValidHeaderLine(name, value), _*) => extractHeaders(lines.tail, Header(lines.head) +: acc)
             case _ => acc
         }
         extractHeaders(rawLines, Array())
     }

    private def bodyFromRawLines(rawLines: Array[String]): Array[String] = {
        @tailrec def extractBody(lines: Array[String]): Array[String] = lines match {
            case Array(ValidHeaderLine(name, value), _*) => extractBody(lines.tail)
            case Array(EmptyLine(line), _*) => lines.tail
            case _ => Array()
        }
        extractBody(rawLines)
    }

    private def respond = {
        if (rawResponse == None) {
            rawResponse = Some(method match {
                case "GET" => new HttpGetResponse(this)
                case "HEAD" => new HttpHeadResponse(this)
                case _ => new HttpNotImplementedErrorResponse(this)
            })
        }
    }

    private def parse = {
        try {
            val rawLines = data.utf8String.split(EOL)
            allLines = Some(rawLines)
            requestLine = RequestLine(rawLines.head)
            rawHeaders = headersFromRawLines(rawLines.tail)
            rawBody = bodyFromRawLines(rawLines.tail)
        } catch {
            case e: Exception => rawResponse = Some(new HttpInternalServerErrorResponse(this))
        }
    }

    override def toString = s"Request: ${method} for ${requestUri}"

    parse 
}

// Response Messages (6)
abstract class HttpResponse(val request: HttpRequest) extends HttpMessage {
    import ParseTokens._
    import HttpServerConfig._

    private val rawBody: Option[Array[String]] = None

    def status = Status(200)
    def data = request.data
    def body: Array[String] = rawBody.getOrElse(Array(""))
    // Status line (6.1)
    def statusLine = "HTTP/1.1" ++ SP ++ status.code.toString ++ SP ++ status.reason
    def headerLines = 
        // Content-Type header (14.17)
        Header("Content-Type", "text/html; charset=utf-8").toString ++ CRLF ++
        // Cache-Control header (14.9)
        Header("Cache-Control", "no-cache").toString ++ CRLF ++
        // Date header (3.3.1), (14.18)
        Header("Date", new java.util.Date().toString).toString ++ CRLF ++
        // Server header (14.9)
        Header("Server", SERVER_HEADER).toString ++ CRLF ++
        // Connection header (14.10)
        Header("Connection", "close").toString ++ CRLF
    def contents: String = {
        statusLine ++ CRLF ++
        headerLines ++
        CRLF ++
        body.mkString(CRLF)
    }
    def result: ByteString = ByteString(contents)
}

class HttpGetResponse(request: HttpRequest, body: Array[String]) extends HttpResponse(request) {
    def this(request: HttpRequest) = this(request, Array())
    override def body: Array[String] = Array("<!DOCTYPE html>", "<html>", "<h1>Request:</h1>", "<pre>") ++
                                       request.lines ++
                                       Array("</pre>", "<h1>Response:</h1>", "<pre>", statusLine, headerLines) ++
                                       Array("</pre>", "</html>")
}

class HttpHeadResponse(request: HttpRequest) extends HttpResponse(request) {
    override def body: Array[String] = Array()
}

// Error Responses
abstract class HttpErrorResponse(request: HttpRequest) extends HttpResponse(request)

class HttpNotImplementedErrorResponse(request: HttpRequest) extends HttpResponse(request) {
    override def status = Status(501)
}

class HttpInternalServerErrorResponse(request: HttpRequest) extends HttpResponse(request) {
    override def status = Status(500)
}

// Status Code Definitions (10)
case class Status(code: Int, reason: String) {
    import ParseTokens._
    override def toString = code + SP + reason
}
object Status {
    def apply(code: Int) = code match {
        case 100 => new Status(100, "Continue")
        case 101 => new Status(101, "Switching Protocols")
        case 200 => new Status(200, "OK")
        case 201 => new Status(201, "Created")
        case 202 => new Status(202, "Accepted")
        case 203 => new Status(203, "Non-Authoritative Information")
        case 204 => new Status(204, "No Content")
        case 205 => new Status(205, "Reset Content")
        case 206 => new Status(206, "Partial Content")
        case 300 => new Status(300, "Multiple Choices")
        case 301 => new Status(301, "Moved Permanently")
        case 302 => new Status(302, "Found")
        case 303 => new Status(303, "See Other")
        case 304 => new Status(304, "Not Modified")
        case 305 => new Status(305, "Use Proxy")
        case 307 => new Status(307, "Temporary Redirect")
        case 400 => new Status(400, "Bad Request")
        case 401 => new Status(401, "Unauthorized")
        case 402 => new Status(402, "Payment Required")
        case 403 => new Status(403, "Forbidden")
        case 404 => new Status(404, "Not Found")
        case 405 => new Status(405, "Method Not Allowed")
        case 406 => new Status(406, "Not Acceptable")
        case 407 => new Status(407, "Proxy Authentication Required")
        case 408 => new Status(408, "Request Time-out")
        case 409 => new Status(409, "Conflict")
        case 410 => new Status(410, "Gone")
        case 411 => new Status(411, "Length Required")
        case 412 => new Status(412, "Precondition Failed")
        case 413 => new Status(413, "Request Entity Too Large")
        case 414 => new Status(414, "Request-URI Too Large")
        case 415 => new Status(415, "Unsupported Media Type")
        case 416 => new Status(416, "Requested range not satisfiable")
        case 417 => new Status(417, "Expectation Failed")
        case 500 => new Status(500, "Internal Server Error")
        case 501 => new Status(501, "Not Implemented")
        case 502 => new Status(502, "Bad Gateway")
        case 503 => new Status(503, "Service Unavailable")
        case 504 => new Status(504, "Gateway Time-out")
        case 505 => new Status(505, "HTTP Version not supported")
    }
}
