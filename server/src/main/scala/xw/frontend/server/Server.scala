package xw.frontend.server

import scala.concurrent.ExecutionContext

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.log4s.getLogger
import rocks.heikoseeberger.accessus.Accessus.RouteOps

import xw.frontend.server.AccessLog.accessLog

object Server {
  private[this] val log = getLogger

  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("frontend-server")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    val route = Routes.root.withTimestampedAccessLog(accessLog)

    Http().bindAndHandle(route, "0.0.0.0", 8080).foreach { binding ⇒
      log.info(s"Server started up on ${binding.localAddress}")
    }
  }
}
