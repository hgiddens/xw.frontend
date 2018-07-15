package xw.frontend
package server

import akka.http.scaladsl.model.{MediaTypes, StatusCodes}
import akka.http.scaladsl.model.headers.{`Accept-Encoding`, `Content-Encoding`}
import akka.http.scaladsl.model.headers.HttpEncodings.{gzip, identity}
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.TestUtils.{gunzipToString, resourceContent}

object RoutesSpec extends Specification with Specs2RouteTest {
  "client" should {
    trait ClientScope extends Scope {
      final val clientJS = "frontend-client-opt.js"
      private val Right(config) = ResourceConfig()
      final val route = Routes.root(config)
      final val request = Get(s"/${config.staticRoot}/$clientJS")
    }

    "be available at the expected location" in new ClientScope {
      request ~> route ~> check {
        status must_=== StatusCodes.OK
      }
    }

    "have the expected media type" in new ClientScope {
      request ~> route ~> check {
        mediaType must_=== MediaTypes.`application/javascript`
      }
    }

    "have expected content when gzip disabled" in new ClientScope {
      request ~> `Accept-Encoding`(identity) ~> route ~> check {
        val actual = responseAs[String]
        val expected = resourceContent(s"/${BuildInfo.webPackageDirectory}/$clientJS").get
        actual.length must_=== expected.length
        actual must_=== expected
      }
    }

    "be served compressed when accept encoding specified" in new ClientScope {
      request ~> `Accept-Encoding`(gzip) ~> route ~> check {
        header[`Content-Encoding`] must beSome.which(_.encodings must_=== Seq(gzip))
      }
    }

    "have compressed content the same as the uncompressed content" in new ClientScope {
      request ~> `Accept-Encoding`(gzip) ~> route ~> check {
        val actual = gunzipToString(responseAs[Array[Byte]])
        val expected = resourceContent(s"/${BuildInfo.webPackageDirectory}/$clientJS").get
        actual must beSome.which(_.length must_=== expected.length)
        actual must beSome(expected)
      }
    }
  }
}
