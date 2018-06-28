package xw.frontend
package server

import java.io.{ByteArrayInputStream, InputStream}
import java.util.zip.GZIPInputStream
import scala.io.Source
import scala.util.Try

object TestUtils {

  /** The string representation of the resource a the given path. */
  def resourceContent(path: String): Option[String] =
    Option(getClass.getResourceAsStream(path)).flatMap(readStream)

  /** Reads an input stream into a string. */
  def readStream(stream: InputStream): Option[String] =
    Try(Source.fromInputStream(stream).mkString).toOption

  /** Gunzips the given bytes, returns the string represented. */
  def gunzipToString(bytes: Array[Byte]): Option[String] =
    readStream(new GZIPInputStream(new ByteArrayInputStream(bytes)))
}
