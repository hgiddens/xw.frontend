package xw.frontend
package server

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
    pathPrefix(config.staticRoot) {
      path(config.clientDeps) {
        getFromResource(config.clientDeps)
      } ~ path(config.client) {
        getFromResource(config.client)
      }
    }

}
