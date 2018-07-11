package xw.frontend
package resources.config

import java.io.File
import scala.io.Source
import scala.util.Try

import cats.implicits._
import io.circe.parser.decode

/** Asset lookup methods compatible with the ScalaJS script library lookup. */
trait AssetLookup {

  /** The public name of an asset. */
  def asset(name: String): String

  /** Whether an asset exists with a given name. */
  def exists(name: String): Boolean
}

final class ResourceConfig(digestFor: String ⇒ String) extends AssetLookup { self ⇒

  /** Asset lookup compatible with the sbt-digest plugin. */
  object digest extends AssetLookup {
    def asset(name: String): String =
      self.asset(digestFor(name))

    def exists(name: String): Boolean =
      self.exists(digestFor(name))
  }

  def asset(name: String): String =
    s"/$staticRoot/$name"

  def exists(name: String): Boolean =
    getClass.getResource(s"/${BuildInfo.webPackageDirectory}/$name") != null

  /** If the path contains a digest, returns it. */
  // TODO: load from index also
  def digestFrom(path: String): Option[String] = {
    val file = new File(path)
    val md5Length = 32
    if (file.getName.length > md5Length && file.getName.charAt(md5Length) === '-') {
      Some(file.getName.substring(0, md5Length))
    } else None
  }

  val title = "xw"

  val staticRoot = "static"
}
object ResourceConfig {

  // TODO: Blocking/errors
  def apply(): ResourceConfig = {
    val path = s"/${BuildInfo.webPackageDirectory}/${BuildInfo.digestIndex}"
    val result = for {
      resource ← Option(getClass.getResource(path))
      asString ← Try(Source.fromURL(resource).mkString.trim).toOption
      asMap ← decode[Map[String, String]](asString).toOption
      fixedMap = asMap.map {
        case (k, v) ⇒
          val stripped = if (v.startsWith("/")) v.substring(1) else v
          (k, stripped)
      }
    } yield new ResourceConfig(name ⇒ fixedMap.getOrElse(name, name))

    result.get
  }
}
