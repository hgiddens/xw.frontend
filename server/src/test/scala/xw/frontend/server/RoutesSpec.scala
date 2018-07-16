package xw.frontend
package server

import akka.http.scaladsl.model.{MediaTypes, StatusCodes}
import akka.http.scaladsl.model.headers.{`Accept-Encoding`, `Content-Encoding`}
import akka.http.scaladsl.model.headers.HttpEncodings.{gzip, identity}
import akka.http.scaladsl.testkit.Specs2RouteTest
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport.jsonUnmarshaller
import io.circe.Json
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.Scope

import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.TestUtils.{asUUID, gunzipToString, resourceContent}
import xw.frontend.server.documents.VarDocumentStore

// TODO: can no longer run these tests from IDEA
object RoutesSpec extends Specification with ScalaCheck with Specs2RouteTest {
  "client" should {
    trait ClientScope extends Scope {
      final val clientJS = "frontend-client-opt.js"
      private val Right(config) = ResourceConfig()
      final val route = Routes.static(config)
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

  "documents" should {
    "getting the list" should {
      "return the documents as JSON" in prop { store: VarDocumentStore ⇒
        Get("/documents") ~> Routes.documents(store) ~> check {
          val documents = responseAs[Json]
          val ids = documents.findAllByKey("id").flatMap(_.asString.flatMap(asUUID)).toVector

          status must_=== StatusCodes.OK
          mediaType must_=== MediaTypes.`application/json`
          ids must_=== store.documents.map(_.id)
        }
      }
    }
  }
}
