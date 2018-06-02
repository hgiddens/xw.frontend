package xw

import scala.{Nothing, inline}

package object frontend extends Imports {
  @inline
  def ??? : Nothing =
    scala.Predef.???

  @inline
  def implicitly[A](implicit ev: A): A =
    ev

  object System {
    @inline
    def nanoTime(): Long = java.lang.System.nanoTime()
  }
}
