package com.emarsys.logger

import java.util.UUID

import ch.qos.logback.classic.{Level, Logger}
import com.emarsys.logger.loggable.{LoggableIntegral, LoggableList, LoggableObject}
import com.emarsys.logger.testutil.TestAppender
import net.logstash.logback.marker.Markers
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.{LoggerFactory, Marker}

import scala.collection.JavaConverters._

trait LoggingBehavior[F[_]] { this: AnyFlatSpec with Matchers with TypeCheckedTripleEquals =>
  type SimpleLogFn       = (Logging[F], String, LoggingContext) => F[Unit]
  type ErrorLogFn        = (Logging[F], Throwable, LoggingContext) => F[Unit]
  type ErrorLogWithMsgFn = (Logging[F], Throwable, String, LoggingContext) => F[Unit]

  def runF(f: F[Unit]): Unit
  def createLogger(name: String): Logging[F]

  def simpleLog(level: Level, logFn: SimpleLogFn): Unit = {

    it should "log with the correct level" in new LoggingScope {
      val ctx = LoggingContext("trid")
      runF(logFn(logger, "message", ctx))

      appender.events.head.getLevel should ===(level)
    }

    it should "log the correct message" in new LoggingScope {
      val ctx = LoggingContext("trid")
      runF(logFn(logger, "message", ctx))

      appender.events.head.getMessage should ===("message")
    }

    it should "log the transaction id" in new LoggingScope {
      val ctx = LoggingContext("trid")
      runF(logFn(logger, "message", ctx))

      appender.events.head.getMarker should ===(Markers.append("transactionId", "trid"))
    }

    it should "log the extended context" in new LoggingScope {
      val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1)))
      runF(logFn(logger, "message", ctx))

      private val marker = flattenMarkers(appender.events.head.getMarker)

      marker(1) should ===(Markers.appendEntries(Map("id" -> 1L).asJava))
    }

    it should "log nested objects in ctx" in new LoggingScope {
      val ctx = LoggingContext("trid", LoggableObject("obj" -> LoggableObject("id" -> LoggableIntegral(1))))
      runF(logFn(logger, "message", ctx))

      private val marker = flattenMarkers(appender.events.head.getMarker)

      marker(1).toString should ===("{obj={id=1}}")
    }

    it should "log list of objects in ctx" in new LoggingScope {
      val ctx = LoggingContext(
        "trid",
        LoggableObject(
          Map(
            "list" -> LoggableList(
              LoggableObject("id" -> LoggableIntegral(1)),
              LoggableObject("id" -> LoggableIntegral(2))
            )
          )
        )
      )
      runF(logFn(logger, "message", ctx))

      private val marker = flattenMarkers(appender.events.head.getMarker)

      marker(1).toString should ===("{list=[{id=1}, {id=2}]}")
    }
  }

  def errorLog(log: ErrorLogFn, logWithMsgFn: ErrorLogWithMsgFn): Unit = {
    it should "log the error in the context" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      runF(log(logger, error, ctx))

      private val marker = flattenMarkers(appender.events.head.getMarker)

      val errorMarker = Markers.appendEntries(
        Map(
          "exception" -> Map(
            "class"      -> error.getClass.getCanonicalName,
            "message"    -> error.getMessage,
            "stacktrace" -> error.getStackTrace.mkString("\n")
          ).asJava
        ).asJava
      )
      marker(1).toString should ===(errorMarker.toString)
    }

    it should "log the error in the context even when logging a message" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      runF(logWithMsgFn(logger, error, "message", ctx))

      private val marker = flattenMarkers(appender.events.head.getMarker)

      val errorMarker = Markers.appendEntries(
        Map(
          "exception" -> Map(
            "class"      -> error.getClass.getCanonicalName,
            "message"    -> error.getMessage,
            "stacktrace" -> error.getStackTrace.mkString("\n")
          ).asJava
        ).asJava
      )
      marker(1).toString should ===(errorMarker.toString)
    }

    it should "log the correct message even when logging error" in new LoggingScope {
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")
      runF(logWithMsgFn(logger, error, "message", ctx))

      appender.events.head.getMessage should ===("message")
    }
  }

  private def flattenMarkers(marker: Marker): List[Marker] =
    if (!marker.iterator().hasNext) {
      List(marker)
    } else {
      marker :: marker.iterator().asScala.flatMap(flattenMarkers).toList
    }

  trait LoggingScope {

    val loggerName         = UUID.randomUUID().toString.take(8)
    val logger: Logging[F] = createLogger(loggerName)

    val appender = new TestAppender
    appender.start()

    private val underlyingLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
    underlyingLogger.detachAndStopAllAppenders()
    underlyingLogger.addAppender(appender)
  }

}
