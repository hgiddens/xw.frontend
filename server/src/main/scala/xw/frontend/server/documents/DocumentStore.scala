package xw.frontend
package server.documents

/** Interface for a set of documents. */
// TODO: document invariants e.g. unique IDs
trait DocumentStore[F[_]] {
  def documents: F[Vector[Document]]
  def addDocument(document: Document): Unit
}
