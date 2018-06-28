package xw.frontend
package server

import akka.http.scaladsl.model.headers.EntityTag
import akka.http.scaladsl.model.headers.HttpEncodings.gzip
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default

import xw.frontend.resources.html
import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.Marshalling._

object Routes {
  def root(config: ResourceConfig): Route =
    (get & pathEndOrSingleSlash) {
      complete(html.index(config))
    } ~ static(config)

  private def static(config: ResourceConfig): Route =
    pathPrefix(config.staticRoot / Remaining) { file ⇒
      val withCacheHeaders =
        config.digestFrom(file).fold(pass)(digest ⇒ conditional(EntityTag(digest)))

      // TODO: tests for this
      // TODO: max-age
      withCacheHeaders {
        val baseName = s"${BuildInfo.webPackageDirectory}/$file"
        responseEncodingAccepted(gzip) {
          getFromResource(baseName + ".gz")
        } ~ getFromResource(baseName)
      }
    }
}
