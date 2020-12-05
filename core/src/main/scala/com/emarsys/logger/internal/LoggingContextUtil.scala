package com.emarsys.logger.internal
import com.emarsys.logger.LoggingContext
import com.emarsys.logger.loggable._
import net.logstash.logback.marker.Markers._
import org.slf4j.Marker

import scala.collection.JavaConverters._

object LoggingContextUtil {

  def toMarker(loggingContext: LoggingContext): Marker = {
    val transactionIdMarker = append("transactionId", loggingContext.transactionId)

    val contextualData = loggingContext.logData.obj

    if (contextualData.isEmpty) {
      transactionIdMarker
    } else {
      transactionIdMarker.and(appendEntries(toJava(contextualData)))
    }
  }

  private def toJava(logData: Map[String, LoggableValue]): java.util.Map[_, _] =
    logData.map({ case (k, v) => (k, toJava(v)) }).asJava

  private def toJava(lv: LoggableValue): Any = lv match {
    case LoggableIntegral(value) => value
    case LoggableFloating(value) => value
    case LoggableString(value)   => value
    case LoggableBoolean(value)  => value
    case LoggableList(list)      => list.map(toJava).asJava
    case LoggableObject(obj)     => toJava(obj)
    case LoggableNil             => null
  }
}
