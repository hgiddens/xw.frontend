package xw.frontend.server

import akka.http.scaladsl.model.MediaTypes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import play.twirl.api.Html

import xw.frontend.server.Generators._

object MarshallingSpec extends Specification with ScalaCheck with Specs2RouteTest {
  "Twirl HTML marshalling" should {

    def route(content: Html) = {
      import Marshalling.htmlToEntityMarshaller
      complete(content)
    }

    "set the media type" in prop { content: Html ⇒
      Get() ~> route(content) ~> check {
        mediaType must_=== MediaTypes.`text/html`
      }
    }

    "provide the body" in prop { content: Html ⇒
      Get() ~> route(content) ~> check {
        responseAs[String] must_=== content.toString
      }
    }
  }
}
