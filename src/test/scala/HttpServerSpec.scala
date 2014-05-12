package net.araxia.http

import org.scalatest._
import akka.util.ByteString

abstract class UnitSpec extends FlatSpec with Matchers with OptionValues with Inspectors with Inside

object BasicGetRequest {
    override def toString = {
        """GET / HTTP/1.1
Host: localhost:8888
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate
Connection: keep-alive
Pragma: no-cache
Cache-Control: no-cache

"""
    }
}

object BasicHeadRequest {
    override def toString = {
        """HEAD / HTTP/1.1
Host: localhost:8888
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate
Connection: keep-alive
Pragma: no-cache
Cache-Control: no-cache

"""
    }
}

object BasicPostRequest {
    override def toString = {
        """POST / HTTP/1.1
Host: localhost:8888
User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:29.0) Gecko/20100101 Firefox/29.0
Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8
Accept-Language: en-US,en;q=0.5
Accept-Encoding: gzip, deflate
Connection: keep-alive
Pragma: no-cache
Cache-Control: no-cache

post body here"""
    }
}
class HttpServerSpec extends UnitSpec {
    val getRequest = new HttpRequest(ByteString(BasicGetRequest.toString))
    val headRequest = new HttpRequest(ByteString(BasicHeadRequest.toString))
    val postRequest = new HttpRequest(ByteString(BasicPostRequest.toString))

    "A GET request" should "have a method" in {
        getRequest.method should be ("GET")
    }

    it should "have a URI" in {
        getRequest.requestUri should be ("/")
    }

    it should "have a protocol version" in {
        getRequest.protocolVersion should be ("HTTP/1.1")
    }

    it should "have headers" in {
        getRequest.headers.size should be (8)
    }

    it should "not have a body" in {
        getRequest.body should be (Array())
    }

    it should "have a response" in {
        getRequest.response.isInstanceOf[HttpResponse] should be (true)
    }

    it should "be OK" in {
        getRequest.response.status.code should be (200)
    }

    it should "have a response body" in {
        println("Contents: " + getRequest.response.contents)
        getRequest.response.body should not be (empty)
    }

    "A HEAD request" should "have a method" in {
        headRequest.method should be ("HEAD")
    }

    it should "have a URI" in {
        headRequest.requestUri should be ("/")
    }

    it should "have a protocol version" in {
        headRequest.protocolVersion should be ("HTTP/1.1")
    }

    it should "have headers" in {
        headRequest.headers.size should be (8)
    }

    it should "not have a body" in {
        headRequest.body should be (Array())
    }

    it should "have a response" in {
        headRequest.response.isInstanceOf[HttpResponse] should be (true)
    }

    it should "be OK" in {
        headRequest.response.status.code should be (200)
    }

    it should "not have a response body" in {
        headRequest.response.body should be (empty)
    }

    "A POST request" should "be unsupported" in {
        postRequest.response.status.code should be (501)
    }

}
