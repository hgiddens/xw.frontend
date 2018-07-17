package xw.frontend
package server

import java.util.UUID

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers.{EntityTag, Location}
import akka.http.scaladsl.model.headers.HttpEncodings.gzip
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.directives.ContentTypeResolver.Default
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import xw.frontend.resources.html
import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.Marshalling._
import xw.frontend.server.documents.{Document, DocumentStore}

object Routes {
  def root(config: ResourceConfig, documentStore: DocumentStore): Route =
    (get & pathEndOrSingleSlash) {
      complete(html.index(config))
    } ~ static(config) ~ documents(documentStore)

  /** Routes for the document API. */
  // TODO: API documentation
  private[server] def documents(documentStore: DocumentStore): Route = {
    val root = "documents"
    path(root) {
      get {
        complete(documentStore.documents)
      } ~ post {
        // TODO: it's almost impossible to test the conflict thing
        // at least, with any sort of reasonable implementation; this indicates that straight up
        // returning unit is probably the way to go – I do want to read up on this tho
        val uuid = UUID.randomUUID()
        val document = Document(uuid)
        val succeeded = documentStore.addDocument(document)
        if (!succeeded) complete(StatusCodes.Conflict)
        else {
          val uri = Uri(path = Path / root / uuid.toString)
          respondWithHeader(Location(uri)) {
            complete(StatusCodes.Created)
          }
        }
      }
    } ~ path(root / JavaUUID) { id ⇒
      documentStore.documents
        .find(_.id == id)
        .fold(complete(StatusCodes.NotFound))(complete(_))
    }
  }

  /** Routes for static content. */
  private[server] def static(config: ResourceConfig): Route =
    pathPrefix(config.staticRoot / Remaining) { file ⇒
      val withCacheHeaders =
        config.digestFrom(file).fold(pass)(digest ⇒ conditional(EntityTag(digest)))

      // TODO: tests for this
      // TODO: max-age
      withCacheHeaders {
        val baseName = s"${BuildInfo.webPackageDirectory}/$file"
        responseEncodingAccepted(gzip) {
          getFromResource(baseName + ".gz")
        } ~ getFromResource(baseName)
      }
    }
}
