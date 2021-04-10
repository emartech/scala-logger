package com.emarsys.logger.loggable

trait GenericLoggableEncoder {
  def derived[A]: LoggableEncoder[A] = ???
}