package xw.frontend
package server

import scala.concurrent.Future
import scala.concurrent.duration.{Duration, FiniteDuration}

import akka.Done
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.headers.{
  Referer,
  `Remote-Address`,
  `User-Agent`,
  `X-Forwarded-For`,
  `X-Real-Ip`
}
import akka.stream.scaladsl.Sink
import org.log4s.getLogger
import rocks.heikoseeberger.accessus.Accessus.AccessLog

object AccessLog {
  private[this] val log = getLogger

  // visible for testing
  def logEntry(req: HttpRequest, res: HttpResponse, duration: FiniteDuration): String = {
    val builder = new StringBuilder(1024)

    // Lookup method based on MiscDirectives._extractClientIP
    val remoteAddress = req
      .header[`X-Forwarded-For`]
      .flatMap(_.addresses.headOption)
      .orElse(
        req.header[`Remote-Address`].map(_.address).orElse(req.header[`X-Real-Ip`].map(_.address))
      )

    remoteAddress.flatMap(_.toOption) match {
      case Some(address) ⇒ builder.append(address.getHostAddress)
      case None ⇒ builder.append('-')
    }

    builder.append(' ')

    builder.append(f"${duration.toMicros / 1000.0}%.03f")

    builder.append(' ')

    builder.append(req.method.value)

    builder.append(' ')

    builder.append('"')
    builder.append(req.uri)
    builder.append('"')

    builder.append(' ')

    builder.append(res.status.intValue)

    builder.append(' ')

    res.entity.contentLengthOption match {
      case Some(length) ⇒ builder.append(length)
      case None ⇒ builder.append('-')
    }

    builder.append(' ')

    req.header[Referer] match {
      case Some(referer) ⇒
        builder.append('"')
        builder.append(referer.uri)
        builder.append('"')

      case None ⇒
        builder.append('-')
    }

    builder.append(' ')

    req.header[`User-Agent`] match {
      case Some(ua) ⇒
        builder.append('"')
        builder.append(ua.value)
        builder.append('"')

      case None ⇒
        builder.append('-')
    }

    builder.toString
  }

  def accessLog: AccessLog[Long, Future[Done]] =
    Sink.foreach {
      case ((req, start), res) ⇒
        val end = System.nanoTime()
        log.info(logEntry(req, res, Duration.fromNanos(end - start)))
    }
}
