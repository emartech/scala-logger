package com.emarsys.logger

import com.emarsys.logger.loggable.{LoggableEncoder, LoggableObject}

case class LoggingContext private (transactionId: String, logData: LoggableObject) {
  import cats.syntax.all._
  import LoggableEncoder.ops._

  def <>[T: LoggableEncoder](param: (String, T)): LoggingContext = addParameter(param)

  def addParameter[T: LoggableEncoder](param: (String, T)): LoggingContext = {
    val encodedParam = param.map(_.toLoggable)
    copy(logData = LoggableObject(logData.obj + encodedParam))
  }
}

object LoggingContext {
  def apply(transactionId: String): LoggingContext = LoggingContext(transactionId, LoggableObject.empty)
}
