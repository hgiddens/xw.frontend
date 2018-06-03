package xw.frontend
package resources.config

final case class ResourceConfig() {
  val title = "xw"

  val staticRoot = "static"
  val clientDeps = "frontend-client-jsdeps.min.js"
  val client = "frontend-client.js"

  type AbstractPath = Seq[String]
  val javascripts: Seq[AbstractPath] =
    Seq(
      Seq(staticRoot, clientDeps),
      Seq(staticRoot, client),
    )
}
