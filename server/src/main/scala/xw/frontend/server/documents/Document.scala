package xw.frontend
package server.documents

import java.util.UUID

import cats.Eq
import io.circe.{Decoder, Encoder, Json}
import io.circe.syntax._

/** A document that can be annotated. */
final case class Document(id: UUID)
object Document {
  implicit val decoder: Decoder[Document] =
    Decoder.instance(_.get[UUID]("id").map(apply))

  implicit val encoder: Encoder[Document] =
    Encoder.instance { document ⇒
      Json.obj(
        "id" := document.id
      )
    }

  implicit val eq: Eq[Document] =
    Eq.fromUniversalEquals
}
