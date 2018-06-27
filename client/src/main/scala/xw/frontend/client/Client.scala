package xw.frontend
package client

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits._

import cats.effect.IO
import cats.implicits._
import io.circe.Json
import hammock.{Hammock, Method, Uri, UriStringContext}
import hammock.js.Interpreter
import japgolly.scalajs.react.{Callback, CallbackTo, ScalaComponent}
import japgolly.scalajs.react.vdom.html_<^._
import org.scalajs.dom.document
import org.scalajs.dom.raw.{EventSource, MessageEvent}

object Button {
  type Props = (String, Callback)

  val component =
    ScalaComponent
      .builder[Props]("CounterButton")
      .render_P { case (title, callback) ⇒ <.button(^.onClick --> callback, title) }
      .build
}

object LiveCounter {
  type Props = String
  type State = (Option[String], Option[EventSource])

  val component = ScalaComponent
    .builder[Props]("LiveCounterComponent")
    .initialState((None: Option[String], None: Option[EventSource]))
    .render_S {
      case (Some(count), Some(_)) ⇒
        <.h2(s"Count: $count")
      case (None, Some(_)) ⇒
        <.h2("No events yet")
      case (_, None) ⇒
        <.h2("Not connected")
    }
    .componentDidMount { c ⇒
      scala.Predef.println("mounting")
      val eventSource = new EventSource(c.props)
      eventSource.onmessage = { event: MessageEvent ⇒
        val data = event.data.asInstanceOf[String]
        scala.Predef.println(s"data: $data")
        if (data.nonEmpty) {
          c.modState { s ⇒
              (Some(data), s._2)
            }
            .runNow()
        } else Callback(())
      }
      c.modState { s ⇒
        (s._1, Some(eventSource))
      }
    }
    .componentWillUnmount { c ⇒
      scala.Predef.println("unmounting")
      Callback(c.state._2.foreach { source ⇒
        source.onmessage = _ ⇒ ()
        source.close()
      })
    }
    .build
}

object MagicButton {
  type Props = Interpreter[IO]

  // This legit looks like the best way
  private def ioCallback(action: IO[Callback]): Callback =
    Callback.future(action.unsafeToFuture())

  private def update(interpreter: Interpreter[IO]): IO[Unit] = {
    def request = Hammock.request(Method.POST, uri"/counter", Map.empty)
    implicit def ix: Interpreter[IO] = interpreter
    request.exec[IO].void
  }

  val component = ScalaComponent
    .builder[Props]("MagicButton")
    .initialState(true)
    .renderPS { ($, interpreter, shown) ⇒
      val action = update(interpreter).map(value ⇒ Callback(()))
      val otherAction = $.modState(shown ⇒ !shown)
      val incrementButton = Button.component("Click", ioCallback(action))

      <.div(
        LiveCounter.component("/counter"),
        Some(incrementButton).filter(_ ⇒ shown),
        Button.component("Toggle", otherAction),
      )
    }
    .build
}

object Client {
  def main(args: Array[String]): Unit = {
    val counter = MagicButton.component(Interpreter[IO])
    counter.renderIntoDOM(document.getElementById("content-root"))
  }
}
