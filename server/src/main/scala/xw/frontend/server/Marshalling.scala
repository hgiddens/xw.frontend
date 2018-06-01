package xw.frontend.server

import akka.http.scaladsl.marshalling.{Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`text/html`
import play.twirl.api.Html

object Marshalling {
  implicit val htmlToEntityMarshaller: ToEntityMarshaller[Html] =
    Marshaller.stringMarshaller(`text/html`).compose(_.toString)
}
