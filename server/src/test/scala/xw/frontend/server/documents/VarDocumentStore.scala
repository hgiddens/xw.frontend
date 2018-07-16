package xw.frontend
package server.documents

import org.scalacheck.{Arbitrary, Gen}

import xw.frontend.server.documents.Generators._

/** A document store implemented by a synchronised var. */
final class VarDocumentStore(private[this] var docs: Vector[Document]) extends DocumentStore {

  def documents: Vector[Document] = docs
}
object VarDocumentStore {
  implicit def arbitrary: Arbitrary[VarDocumentStore] =
    Arbitrary {
      for {
        // TODO: replace with Arbitrary.arbitrary[Vector[Document]] when the predef is back
        documents ← Gen.listOf(Arbitrary.arbitrary[Document])
      } yield new VarDocumentStore(documents.toVector)
    }
}
