package xw.frontend
package server.documents

/** Interface for a set of documents. */
trait DocumentStore {
  def documents: Vector[Document]
}
