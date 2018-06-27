package xw.frontend
package server

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.sys

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import org.log4s.getLogger
import pureconfig.loadConfig
import pureconfig.error.ConfigReaderFailures
import rocks.heikoseeberger.accessus.Accessus.RouteOps

import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.AccessLog.accessLog

object Server {
  final private case class Config(shutdownDeadline: FiniteDuration)

  private[this] val log = getLogger

  private def withConfig(config: Config): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("frontend-server")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    val resourceConfig = ResourceConfig()
    val route = Routes.root(resourceConfig).withTimestampedAccessLog(accessLog)

    Http().bindAndHandle(route, "0.0.0.0", 8080).foreach { binding ⇒
      sys.addShutdownHook {
        log.debug("Termination signal received")
        // Heroku imposes a 30 second timeout; don't bet the farm.
        binding
          .terminate(config.shutdownDeadline)
          .flatMap { _ ⇒
            log.debug("Shutting down materializer")
            materializer.shutdown()
            log.debug("Shutting down actor system")
            actorSystem.terminate()
          }
          .foreach(_ ⇒ log.debug("Shutdown completed"))
      }

      log.info(s"Server started up on ${binding.localAddress}")
    }
  }

  private def onConfigError(failures: ConfigReaderFailures): Unit = {
    failures.toList.foreach { failure ⇒
      val location = failure.location.map(location ⇒ " " + location.description).getOrElse("")
      log.error(s"${failure.description}$location")
    }
    sys.exit(1)
  }

  def main(args: Array[String]): Unit =
    loadConfig[Config]("server").fold(onConfigError, withConfig)
}
