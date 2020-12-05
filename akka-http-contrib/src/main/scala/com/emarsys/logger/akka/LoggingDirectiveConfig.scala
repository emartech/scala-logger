package com.emarsys.logger.akka

case class LoggingDirectiveConfig(requestIdHeader: String = "X-Request-ID", requestIdFieldName: String = "requestId")
