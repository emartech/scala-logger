package com.emarsys.logger

import cats.Id
import ch.qos.logback.classic.{Level, Logger}
import com.emarsys.logger.testutil.TestAppender
import net.logstash.logback.marker.Markers
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.{LoggerFactory, Marker}

import scala.collection.JavaConverters._

class LogbackLoggerInstanceSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals with LoggerBehavior {

  "Logging.debug" should behave like simpleLog(Level.DEBUG, { case (logger, msg, ctx) => logger.debug(msg)(ctx) })

  "Logging.info" should behave like simpleLog(Level.INFO, { case (logger, msg, ctx) => logger.info(msg)(ctx) })

  "Logging.warn" should behave like simpleLog(Level.WARN, { case (logger, msg, ctx) => logger.warn(msg)(ctx) })

  it should behave like errorLog(
    { case (logger, error, ctx)          => logger.warn(error)(ctx) },
    { case (logger, error, message, ctx) => logger.warn(error, message)(ctx) }
  )

  "Logging.error" should behave like simpleLog(Level.ERROR, { case (logger, msg, ctx) => logger.error(msg)(ctx) })

  it should behave like errorLog(
    { case (logger, error, ctx)          => logger.error(error)(ctx) },
    { case (logger, error, message, ctx) => logger.error(error, message)(ctx) }
  )

}

trait LoggerBehavior { this: FlatSpec with Matchers with TypeCheckedTripleEquals =>
  type SimpleLogFn       = (Logging[Id], String, LoggingContext) => Unit
  type ErrorLogFn        = (Logging[Id], Throwable, LoggingContext) => Unit
  type ErrorLogWithMsgFn = (Logging[Id], Throwable, String, LoggingContext) => Unit

  trait LoggingScope {

    val logger: Logging[Id] = Logging.defaultLogging

    val appender = new TestAppender
    appender.start()

    private val underlyingLogger = LoggerFactory.getLogger("default").asInstanceOf[Logger]
    underlyingLogger.detachAndStopAllAppenders()
    underlyingLogger.addAppender(appender)
  }

  def simpleLog(level: Level, logFn: SimpleLogFn): Unit = {
    it should "log with the correct level" in new LoggingScope {
      val ctx = LoggingContext("trid")
      logFn(logger, "message", ctx)

      appender.events.head.getLevel should ===(level)
    }

    it should "log the correct message" in new LoggingScope {
      val ctx = LoggingContext("trid")
      logFn(logger, "message", ctx)

      appender.events.head.getMessage should ===("message")
    }

    it should "log the transaction id" in new LoggingScope {
      val ctx = LoggingContext("trid")
      logFn(logger, "message", ctx)

      appender.events.head.getMarker should ===(Markers.append("transactionId", "trid"))
    }

    it should "log the extended context" in new LoggingScope {
      val ctx = LoggingContext("trid", Map("id" -> 1))
      logFn(logger, "message", ctx)

      private val marker = flattenMarkers(appender.events.head.getMarker)

      marker(1) should ===(Markers.appendEntries(Map("id" -> 1).asJava))
    }
  }

  def errorLog(log: ErrorLogFn, logWithMsgFn: ErrorLogWithMsgFn): Unit = {
    it should "log the error in the context" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      log(logger, error, ctx)

      private val marker = flattenMarkers(appender.events.head.getMarker)

      val errorMarker = Markers.appendEntries(
        Map(
          "exception" -> Map(
            "class"      -> error.getClass,
            "message"    -> error.getMessage,
            "stacktrace" -> error.getStackTrace.toSeq.map(_.toString).asJava
          ).asJava
        ).asJava)
      marker(1).toString should ===(errorMarker.toString)
    }

    it should "log the error in the context even when logging a message" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      logWithMsgFn(logger, error, "message", ctx)

      private val marker = flattenMarkers(appender.events.head.getMarker)

      val errorMarker = Markers.appendEntries(
        Map(
          "exception" -> Map(
            "class"      -> error.getClass,
            "message"    -> error.getMessage,
            "stacktrace" -> error.getStackTrace.toSeq.map(_.toString).asJava
          ).asJava
        ).asJava)
      marker(1).toString should ===(errorMarker.toString)
    }

    it should "log the correct message even when logging error" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      logWithMsgFn(logger, error, "message", ctx)

      appender.events.head.getMessage should ===("message")
    }
  }

  private def flattenMarkers(marker: Marker): List[Marker] = {
    if (!marker.iterator().hasNext) {
      List(marker)
    } else {
      marker :: marker.iterator().asScala.flatMap(flattenMarkers).toList
    }
  }

}
