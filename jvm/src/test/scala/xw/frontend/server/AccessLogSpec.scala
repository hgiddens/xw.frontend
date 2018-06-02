package xw.frontend
package server

import java.net.InetAddress
import scala.concurrent.duration._

import akka.http.scaladsl.model.{
  ContentTypes,
  HttpEntity,
  HttpResponse,
  RemoteAddress,
  StatusCodes,
  Uri
}
import akka.http.scaladsl.model.headers.{Referer, `Remote-Address`, `User-Agent`, `X-Forwarded-For`}
import akka.http.scaladsl.testkit.Specs2RouteTest
import akka.stream.scaladsl.Source
import akka.util.ByteString
import org.specs2.mutable.Specification

object AccessLogSpec extends Specification with Specs2RouteTest {
  private def remoteAddress(a: Byte, b: Byte, c: Byte, d: Byte): RemoteAddress =
    RemoteAddress(InetAddress.getByAddress(Array(a, b, c, d)))

  private val duration = 1.millisecond

  "access log entry" should {
    "contain '-' when remote address is unknown" in {
      val req = Get()
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must startWith("-")
    }

    "contain remote ip when no x-forwarded-for is present" in {
      val req = Get() ~> addHeader(`Remote-Address`(remoteAddress(127, 0, 0, 1)))
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("127.0.0.1")
    }

    "contain forwarded address when x-forwarded-for is present" in {
      val req = Get() ~> addHeaders(
        `Remote-Address`(remoteAddress(127, 0, 0, 1)),
        `X-Forwarded-For`(remoteAddress(127, 0, 0, 2), remoteAddress(127, 0, 0, 3))
      )
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("127.0.0.2") and
        not(contain("127.0.0.1")) and
        not(contain("127.0.0.3"))
    }

    "contain request processing duration in ms with us granualrity" in {
      val req = Get()
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, 123456789.nanoseconds) must contain("123.456")
    }

    "contain method" in {
      val req = Get()
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("GET")
    }

    "contain path" in {
      val req = Get("/path")
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("\"/path\"")
    }

    "contain status" in {
      val req = Get()
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("200")
    }

    "contain bytes returned for entity of known size" in {
      val req = Get()
      val entity = HttpEntity(List.fill(123)("0").mkString)
      val res = HttpResponse(StatusCodes.OK, entity = entity)

      AccessLog.logEntry(req, res, duration) must contain("123")
    }

    "contain - for bytes returned for entity of unknown size" in {
      val req = Get()
      val byteStrings = List(ByteString(Array(0.toByte)))
      val source = Source.fromIterator(() ⇒ byteStrings.iterator)
      val entity = HttpEntity(ContentTypes.`application/octet-stream`, source)
      val res = HttpResponse(StatusCodes.OK, entity = entity)

      AccessLog.logEntry(req, res, duration) must contain("200 -")
    }

    "contain referrer" in {
      val referer = Uri("http://example.com/stuff")
      val req = Get() ~> addHeader(Referer(referer))
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("\"http://example.com/stuff\"")
    }

    "contain user agent" in {
      val req = Get() ~> addHeader(`User-Agent`("user-agent"))
      val res = HttpResponse(StatusCodes.OK)

      AccessLog.logEntry(req, res, duration) must contain("\"user-agent\"")
    }
  }
}
