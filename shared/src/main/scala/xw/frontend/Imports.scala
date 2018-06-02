package xw.frontend

trait Imports {
  // Primitives
  final type Any = scala.Any
  final type Array[A] = scala.Array[A]
  final val Array: scala.Array.type = scala.Array
  final type Byte = scala.Byte
  final type Long = scala.Long
  final type Unit = scala.Unit

  // Option
  final type Option[A] = scala.Option[A]
  final val Some: scala.Some.type = scala.Some
  final val None: scala.None.type = scala.None

  // Strings
  final type String = java.lang.String
  final type StringBuilder = java.lang.StringBuilder
  final val StringContext: scala.StringContext.type = scala.StringContext

  // Collections
  final type Seq[A] = scala.collection.immutable.Seq[A]
  final val Seq: scala.collection.immutable.Seq.type = scala.collection.immutable.Seq
  final type List[A] = scala.collection.immutable.List[A]
  final val List: scala.collection.immutable.List.type = scala.collection.immutable.List
}
