package xw.frontend
package server.documents

import cats.Id
import org.scalacheck.{Arbitrary, Gen}

import xw.frontend.server.documents.Generators._

/** A document store implemented by a synchronised var. */
// TODO: thread-safe + tests
// TODO: not ID
final class VarDocumentStore private (private[this] var docs: Vector[Document])
    extends DocumentStore[Id] {

  def documents: Vector[Document] = docs

  def addDocument(document: Document): Unit =
    if (!docs.exists(_.id == document.id)) {
      docs = docs :+ document
    }
}
object VarDocumentStore {
  implicit def arbitrary: Arbitrary[VarDocumentStore] =
    Arbitrary {
      for {
        // TODO: replace with Arbitrary.arbitrary[Set[Document]] when the predef is back
        documents ← Gen.listOf(Arbitrary.arbitrary[Document])
      } yield new VarDocumentStore(documents.toVector)
    }
}

/** A VarDocumentStore and a Document that's in the store. */
final case class VarDocumentStoreWithDocument(store: VarDocumentStore, document: Document)
object VarDocumentStoreWithDocument {
  implicit def arbitrary: Arbitrary[VarDocumentStoreWithDocument] =
    Arbitrary {
      for {
        store ← Arbitrary.arbitrary[VarDocumentStore]
        documents = store.documents
        if documents.nonEmpty
        document ← Gen.oneOf(documents)
      } yield apply(store, document)
    }
}
