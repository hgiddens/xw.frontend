package xw.frontend
package server.documents

/** Interface for a set of documents. */
// TODO: document invariants e.g. unique IDs
// TODO: this isn't very cqrs
trait DocumentStore {
  def documents: Vector[Document]

  // TODO: this is garbage but will do for now
  def addDocument(document: Document): Boolean
}
