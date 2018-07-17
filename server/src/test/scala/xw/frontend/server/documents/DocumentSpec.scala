package xw.frontend.server.documents

import io.circe.testing.CodecTests
import io.circe.testing.instances._
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification

import xw.frontend.server.documents.Generators._

object DocumentSpec extends Specification with ScalaCheck {
  "codec" should {
    "roundtrip" in CodecTests[Document].codec.all
  }
}
