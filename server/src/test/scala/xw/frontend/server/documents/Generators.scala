package xw.frontend
package server.documents

import org.scalacheck.{Arbitrary, Gen, Shrink}

// TODO: Magnolia or whatever
object Generators {
  implicit def arbDocument: Arbitrary[Document] =
    Arbitrary(Gen.resultOf(Document.apply _))

  implicit def shrinkDocument: Shrink[Document] =
    Shrink { document ⇒
      Shrink.shrink(document.id).map(id ⇒ document.copy(id = id))
    }
}
