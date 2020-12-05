package com.emarsys.logger.akka

import java.util.UUID

import akka.http.scaladsl.model.{HttpHeader, HttpRequest}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{extractRequest, provide, respondWithHeader}
import com.emarsys.logger.LoggingContext
import com.emarsys.logger.syntax._

trait LoggingDirective {

  type LogEntry = (String, HasLoggableEncoder)

  def withAkkaLoggingContext(requestLogEntries: LogEntry*)(
      loggingContext: LoggingContext = LoggingContext("unknown transactionId"),
      config: LoggingDirectiveConfig = LoggingDirectiveConfig()
  ): Directive1[LoggingContext] =
    extractRequest.flatMap { request =>
      val requestId = generateRequestId(request)(config)
      respondWithHeader(RawHeader(config.requestIdHeader, requestId)) & extendLoggingContext(
        loggingContext,
        request,
        requestId,
        requestLogEntries: _*
      )(config)
    }

  private def generateRequestId(request: HttpRequest)(config: LoggingDirectiveConfig) =
    request.headers.find(_.lowercaseName == config.requestIdHeader.toLowerCase) match {
      case Some(HttpHeader(_, id)) => id
      case None                    => UUID.randomUUID().toString
    }

  private def extendLoggingContext(
      loggingContext: LoggingContext,
      request: HttpRequest,
      requestId: String,
      requestLogEntries: LogEntry*
  )(config: LoggingDirectiveConfig): Directive1[LoggingContext] = {
    val akkaLoggingContext = loggingContext
      .addParameter(config.requestIdFieldName -> requestId)
      .addParameter(routeLogEntry(request))

    provide(akkaLoggingContext.addParameters(requestLogEntries: _*))
  }

  private def routeLogEntry(request: HttpRequest): (String, String) =
    "route" -> s"${request.method.value} ${request.uri.path}"

}

object LoggingDirective extends LoggingDirective
