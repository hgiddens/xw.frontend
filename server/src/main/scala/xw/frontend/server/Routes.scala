package xw.frontend
package server

import scala.collection.immutable.Set
import scala.concurrent.duration._

import akka.actor.{Actor, ActorRef, ActorSystem, Props, Terminated}
import akka.http.scaladsl.marshalling.sse.EventStreamMarshalling._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import akka.stream.{Materializer, OverflowStrategy}
import akka.stream.scaladsl.Source
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.Json
import io.circe.syntax._
import org.log4s.getLogger

import xw.frontend.resources.html
import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.Marshalling._

object Routes {
  private[this] val log = getLogger

  def root(config: ResourceConfig)(implicit system: ActorSystem, m: Materializer): Route = {
    val actor = system.actorOf(Props[Whatever])
    (get & pathEndOrSingleSlash) {
      complete(html.index(config))
    } ~
      static(config) ~
      counter(actor)
  }

  final private case class AddActor(actor: ActorRef)
  final private case object Increment

  final private class Whatever extends Actor {
    private[this] var others = Set.empty[ActorRef]
    private[this] var value = 0

    def receive: Receive = {
      case Increment ⇒
        value += 1
        log.info(s"value incremented to $value")
        val data = Json.obj(("value", value.asJson))
        val event = ServerSentEvent(data.noSpaces, id = Some(value.toString))
        others.foreach { actor ⇒
          actor ! event
        }

      case AddActor(actor) ⇒
        log.info(s"Actor added $actor")
        context.watch(actor)
        others += actor

      case Terminated(actor) ⇒
        log.info(s"Actor died $actor")
        others -= actor
    }
  }

  private def counter(it: ActorRef)(implicit m: Materializer): Route =
    pathPrefix("counter") {
      get {
        complete {
          val source = Source
            .actorRef[ServerSentEvent](1, OverflowStrategy.dropHead)
            .keepAlive(10.second, () ⇒ ServerSentEvent.heartbeat)
          val (actor, otherSource) = source.preMaterialize()
          it ! AddActor(actor)
          otherSource
        }
      } ~ post {
        complete {
          it ! Increment
          StatusCodes.NoContent
        }
      }
    }

  private def static(config: ResourceConfig): Route =
    pathPrefix(config.staticRoot / Remaining) { file ⇒
      getFromResource(BuildInfo.webPackagePrefix + file)
    }
}
