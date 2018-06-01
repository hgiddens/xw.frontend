package xw.frontend.server

import org.scalacheck.{Arbitrary, Shrink}
import play.twirl.api.Html

object Generators {
  implicit def arbitraryHtml: Arbitrary[Html] =
    Arbitrary(Arbitrary.arbitrary[String].map(Html(_)))

  implicit def shrinkHtml: Shrink[Html] =
    Shrink { html ⇒
      Shrink.shrink(html.toString).map(Html(_))
    }
}
