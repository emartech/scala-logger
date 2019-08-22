package com.emarsys.logger.testutil

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase

import scala.collection.mutable.ListBuffer

class TestAppender extends AppenderBase[ILoggingEvent] {
  val events: ListBuffer[ILoggingEvent] = ListBuffer.empty[ILoggingEvent]

  override def append(eventObject: ILoggingEvent): Unit =
    events += eventObject
}
