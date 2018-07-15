package xw.frontend
package resources.config

import java.io.File
import scala.io.Source
import scala.util.Try

import cats.implicits._
import io.circe.parser.decode

/**
  * Resource configuration used by the Twirl templates.
  *
  * Includes support for looking up asset paths that include digests from paths that don't include
  * the digests. It is assumed that digests are used for <i>all</i> static assets.
  */
final class ResourceConfig(digestFor: String ⇒ Option[String]) { self ⇒

  /** The public name of an asset. */
  def publicAssetPath(name: String): String = {
    val withDigest = digestFor(name).getOrElse(name)
    s"/$staticRoot/$withDigest"
  }

  /** Whether an asset exists with a given name. */
  def assetExists(name: String): Boolean =
    digestFor(name).isDefined

  /** If the file component of the path contains a digest, returns it. */
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
    } yield new ResourceConfig(fixedMap.get)

    result.get
  }
}
