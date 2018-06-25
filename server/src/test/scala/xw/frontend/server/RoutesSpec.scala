package xw.frontend
package server

import akka.http.scaladsl.model.{MediaTypes, StatusCodes}
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.mutable.Specification

import xw.frontend.resources.config.ResourceConfig

object RoutesSpec extends Specification with Specs2RouteTest {
  // TODO: test for the dependencies of the client, when they exist?

  "client" should {
    val clientJS = "frontend-client-opt.js"

    "be available at the expected location" in {
      val config = ResourceConfig()
      val route = Routes.root(config)
      val path = s"/${config.staticRoot}/$clientJS"
      Get(path) ~> route ~> check {
        status must_=== StatusCodes.OK
      }
    }

    "have the expected media type" in {
      val config = ResourceConfig()
      val route = Routes.root(config)
      val path = s"/${config.staticRoot}/$clientJS"
      Get(path) ~> route ~> check {
        mediaType must_=== MediaTypes.`application/javascript`
      }
    }
  }
}
