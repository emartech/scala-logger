package com.emarsys.logger

case class LoggingContext(transactionID: String, logData: Map[String, Any] = Map.empty) {
  def +(param: (String, Any)): LoggingContext            = addParameter(param)
  def addParameter(param: (String, Any)): LoggingContext = addParameters(param)

  def ++(parameters: (String, Any)*): LoggingContext = addParameters(parameters: _*)
  def addParameters(parameters: (String, Any)*): LoggingContext =
    LoggingContext(transactionID, logData ++ parameters)
}
