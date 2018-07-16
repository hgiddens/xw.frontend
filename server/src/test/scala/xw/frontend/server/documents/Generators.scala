package xw.frontend
package server.documents

import org.scalacheck.{Arbitrary, Gen}

// TODO: Magnolia or whatever
object Generators {
  implicit def arbDocument: Arbitrary[Document] =
    Arbitrary(Gen.resultOf(Document.apply _))
}
