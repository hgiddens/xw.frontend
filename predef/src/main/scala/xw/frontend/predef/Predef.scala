package xw.frontend.predef

import scala.{Nothing, inline}

object Predef {
  @inline
  def ??? : Nothing =
    scala.Predef.???

  @inline
  def implicitly[A](implicit ev: A): A =
    ev
}
