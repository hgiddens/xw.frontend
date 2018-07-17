package xw.frontend
package server

import akka.http.scaladsl.marshalling.{GenericMarshallers, Marshaller, ToEntityMarshaller}
import akka.http.scaladsl.model.MediaTypes.`text/html`
import cats.Id
import play.twirl.api.Html

import scala.concurrent.Future

object Marshalling {
  trait MarshallingM[F[_]] {
    def marshaller[A, B](implicit m: Marshaller[A, B]): Marshaller[F[A], B]
  }
  object MarshallingM {
    implicit val future: MarshallingM[Future] =
      new MarshallingM[Future] {
        def marshaller[A, B](implicit m: Marshaller[A, B]): Marshaller[Future[A], B] =
          GenericMarshallers.futureMarshaller[A, B]
      }

    implicit def id: MarshallingM[Id] =
      new MarshallingM[Id] {
        def marshaller[A, B](implicit m: Marshaller[A, B]): Marshaller[Id[A], B] =
          m
      }
  }

  implicit val htmlToEntityMarshaller: ToEntityMarshaller[Html] =
    Marshaller.stringMarshaller(`text/html`).compose(_.toString)

  implicit def marshallingM[F[_], A, B](
      implicit m: MarshallingM[F],
      mm: Marshaller[A, B]
  ): Marshaller[F[A], B] =
    m.marshaller[A, B]
}
