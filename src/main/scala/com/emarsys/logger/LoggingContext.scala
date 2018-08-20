package com.emarsys.logger

import com.emarsys.logger.loggable.{LoggableEncoder, LoggableObject}

case class LoggingContext(transactionID: String, logData: LoggableObject = LoggableObject(Map.empty)) {
  import cats.implicits._
  import LoggableEncoder.ops._

  def +[T: LoggableEncoder](param: (String, T)): LoggingContext = addParameter(param)
  def addParameter[T: LoggableEncoder](param: (String, T)): LoggingContext = {
    val encodedParam = param.map(_.toLoggable)
    copy(logData = LoggableObject(logData.obj + encodedParam))
  }
}
