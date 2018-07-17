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
import cats.Functor
import cats.implicits._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._

import xw.frontend.resources.html
import xw.frontend.resources.config.ResourceConfig
import xw.frontend.server.Marshalling._
import xw.frontend.server.documents.{Document, DocumentStore}

object Routes {
  def root[F[_]: Functor: MarshallingM](
      config: ResourceConfig,
      documentStore: DocumentStore[F]
  ): Route =
    (get & pathEndOrSingleSlash) {
      complete(html.index(config))
    } ~ static(config) ~ documents(documentStore)

  /** Routes for the document API. */
  // TODO: API documentation
  private[server] def documents[F[_]: Functor: MarshallingM](
      documentStore: DocumentStore[F]
  ): Route = {
    val root = "documents"
    path(root) {
      get {
        complete(documentStore.documents)
      } ~ post {
        val id = UUID.randomUUID()
        val document = Document(id)
        documentStore.addDocument(document)
        val uri = Uri(path = Path / root / id.toString)
        respondWithHeader(Location(uri)) {
          complete(StatusCodes.Created)
        }
      }
    } ~ path(root / JavaUUID) { id ⇒
      complete {
        for {
          docs ← documentStore.documents
        } yield docs.find(_.id == id).toRight(StatusCodes.NotFound)
      }
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
