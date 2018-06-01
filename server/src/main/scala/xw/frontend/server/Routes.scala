package xw.frontend.server

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route

import xw.frontend.resources.html
import xw.frontend.server.Marshalling._

object Routes {
  def root: Route =
    complete(html.index())
}
