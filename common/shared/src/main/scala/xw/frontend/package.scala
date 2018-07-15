package xw

import scala.inline

/**
  * Logically a predef, but implemented as a top level package object for compatibility with
  * Lightbend Scala.
  */
package object frontend extends Imports {
  @inline
  def ??? : Nothing =
    scala.Predef.???

  @inline
  def implicitly[A](implicit ev: A): A =
    ev

  object System {
    @inline
    def nanoTime(): Long =
      java.lang.System.nanoTime()
  }
}
