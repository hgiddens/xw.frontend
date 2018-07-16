package xw.frontend
package server.documents

import java.util.UUID

import io.circe.{Encoder, Json}
import io.circe.syntax._

/** A document that can be annotated. */
final case class Document(id: UUID)
object Document {
  implicit val encoder: Encoder[Document] =
    Encoder.instance { document ⇒
      Json.obj(
        "id" := document.id.toString
      )
    }
}
