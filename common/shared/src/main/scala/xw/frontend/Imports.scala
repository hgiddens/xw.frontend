package xw.frontend

import scala.collection.immutable

trait Imports {
  // Primitives
  final type Any = scala.Any
  final type Boolean = scala.Boolean
  final type Byte = scala.Byte
  final type Int = scala.Int
  final type Long = scala.Long
  final type Unit = scala.Unit

  // Strings
  final type String = java.lang.String
  final type StringBuilder = java.lang.StringBuilder
  final val StringContext: scala.StringContext.type = scala.StringContext

  // Collections
  final type Array[A] = scala.Array[A]
  final val Array: scala.Array.type = scala.Array

  final type List[A] = immutable.List[A]
  final val List: immutable.List.type = immutable.List

  final type Map[A, B] = immutable.Map[A, B]
  final val Map: immutable.Map.type = immutable.Map

  final type Option[A] = scala.Option[A]
  final val Option: scala.Option.type = scala.Option
  final val Some: scala.Some.type = scala.Some
  final val None: scala.None.type = scala.None

  // TODO: Vector?
  final type Seq[A] = immutable.Seq[A]
  final val Seq: immutable.Seq.type = immutable.Seq
}
