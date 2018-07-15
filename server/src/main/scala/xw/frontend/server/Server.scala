package xw.frontend
package server

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.sys

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import eu.timepit.refined.W
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Interval
import eu.timepit.refined.pureconfig._
import org.log4s.getLogger
import pureconfig.loadConfig
import pureconfig.error.ConfigReaderFailures
import rocks.heikoseeberger.accessus.Accessus.RouteOps

import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.AccessLog.accessLog

object Server {
  private type PortNumber = Interval.Closed[W.`1`.T, W.`65535`.T]
  final private case class Config(
      interface: String,
      port: Int Refined PortNumber,
      shutdownDeadline: FiniteDuration
  )

  private[this] val log = getLogger

  private def withConfig(config: Config, resourceConfig: ResourceConfig): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("frontend-server")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executionContext: ExecutionContext = actorSystem.dispatcher

    val route = Routes.root(resourceConfig).withTimestampedAccessLog(accessLog)

    Http().bindAndHandle(route, config.interface, config.port.value).foreach { binding ⇒
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

  private def onConfigError(failures: ConfigReaderFailures): Nothing = {
    failures.toList.foreach { failure ⇒
      val location = failure.location.map(location ⇒ " " + location.description).getOrElse("")
      log.error(s"${failure.description}$location")
    }
    sys.exit(1)
  }

  private def onError(message: String): Nothing = {
    log.error(message)
    sys.exit(1)
  }

  def main(args: Array[String]): Unit = {
    val config = loadConfig[Config]("server").left.map(onConfigError).merge
    val resourceConfig = ResourceConfig().left.map(onError).merge
    withConfig(config, resourceConfig)
  }
}
