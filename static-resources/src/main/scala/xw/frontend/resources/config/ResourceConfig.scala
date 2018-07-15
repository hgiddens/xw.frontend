package xw.frontend
package resources.config

import java.io.File
import scala.io.Source
import scala.util.Try

import cats.implicits._
import io.circe.parser.decode

/** Resource configuration used by the Twirl templates. */
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

  private def dropSlash(s: String): String =
    if (s.startsWith("/")) s.substring(1)
    else s

  /**
    * Creates a ResourceConfig instance that looks up assets via the digest index.
    *
    * Assets are looked up using paths that include a digest. Lookup of assets that don't include
    * their digest in their file name is not supported; it is assumed that digests are used for
    * <i>all</i> assets.
    */
  def apply(): Either[String, ResourceConfig] = {
    val path = s"/${BuildInfo.webPackageDirectory}/${BuildInfo.digestIndex}"
    for {
      resource ← Option(getClass.getResource(path)).toRight(s"Missing digest index $path")
      asString ← Try(Source.fromURL(resource).mkString.trim).toEither.leftMap(_.getMessage)
      asMap ← decode[Map[String, String]](asString).left.map(_.getMessage)
      fixedMap = asMap.mapValues(dropSlash).view.force
    } yield new ResourceConfig(fixedMap.get)
  }
}
