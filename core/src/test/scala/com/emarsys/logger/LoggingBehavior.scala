package com.emarsys.logger

import cats.Monad
import ch.qos.logback.classic.{Level, Logger}
import com.emarsys.logger.loggable.{LoggableIntegral, LoggableList, LoggableObject}
import com.emarsys.logger.testutil.TestAppender
import munit.FunSuite
import net.logstash.logback.marker.Markers
import org.slf4j.{LoggerFactory, Marker}

import java.util.UUID
import scala.jdk.CollectionConverters._

trait LoggingBehavior[F[_]] { this: FunSuite =>
  import cats.syntax.functor._

  type SimpleLogFn       = (Logging[F], String, LoggingContext) => F[Unit]
  type ErrorLogFn        = (Logging[F], Throwable, LoggingContext) => F[Unit]
  type ErrorLogWithMsgFn = (Logging[F], Throwable, String, LoggingContext) => F[Unit]

  implicit val monadF: Monad[F]

  def createLogger(name: String): Logging[F]

  case class LoggingFixture(logger: Logging[F], appender: TestAppender)

  private val loggingFixture = FunFixture[LoggingFixture](
    setup = { _ =>
      val loggerName         = UUID.randomUUID().toString.take(8)
      val logger: Logging[F] = createLogger(loggerName)

      val appender = new TestAppender
      appender.start()

      val underlyingLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
      underlyingLogger.detachAndStopAllAppenders()
      underlyingLogger.addAppender(appender)

      LoggingFixture(logger, appender)
    },
    teardown = { _ => }
  )

  private def flattenMarkers(marker: Marker): List[Marker] =
    if (!marker.iterator().hasNext) {
      List(marker)
    } else {
      marker :: marker.iterator().asScala.flatMap(flattenMarkers).toList
    }

  def simpleLog(name: String, level: Level, logFn: SimpleLogFn) = {
    loggingFixture.test(s"$name should log with the correct level") { f =>
      val ctx = LoggingContext("trid")

      for {
        _ <- logFn(f.logger, "message", ctx)
      } yield assertEquals(f.appender.events.head.getLevel, level)
    }

    loggingFixture.test(s"$name should log the correct message") { f =>
      val ctx = LoggingContext("trid")
      for {
        _ <- logFn(f.logger, "message", ctx)
      } yield assertEquals(f.appender.events.head.getMessage, "message")
    }

    loggingFixture.test(s"$name should log the transaction id") { f =>
      val ctx = LoggingContext("trid")

      for {
        _ <- logFn(f.logger, "message", ctx)
      } yield assertEquals(f.appender.events.head.getMarker, Markers.append("transactionId", "trid"))
    }

    loggingFixture.test(s"$name should log the extended context") { f =>
      val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1)))

      for {
        _ <- logFn(f.logger, "message", ctx)
        marker = flattenMarkers(f.appender.events.head.getMarker)
      } yield assertEquals(marker(1), Markers.appendEntries(Map("id" -> 1L).asJava))
    }

    loggingFixture.test(s"$name should log nested objects in ctx") { f =>
      val ctx = LoggingContext("trid", LoggableObject("obj" -> LoggableObject("id" -> LoggableIntegral(1))))

      for {
        _ <- logFn(f.logger, "message", ctx)
        marker = flattenMarkers(f.appender.events.head.getMarker)
      } yield assertEquals(marker(1).toString, "{obj={id=1}}")
    }

    loggingFixture.test(s"$name should log list of objects in ctx") { f =>
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

      for {
        _ <- logFn(f.logger, "message", ctx)
        marker = flattenMarkers(f.appender.events.head.getMarker)
      } yield assertEquals(marker(1).toString, "{list=[{id=1}, {id=2}]}")
    }
  }

  def errorLog(name: String, log: ErrorLogFn, logWithMsgFn: ErrorLogWithMsgFn) = {
    loggingFixture.test(s"$name should log the error in the context") { f =>
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")

      for {
        _ <- log(f.logger, error, ctx)
        marker = flattenMarkers(f.appender.events.head.getMarker)
      } yield {
        val errorMarker = Markers.appendEntries(
          Map(
            "exception" -> Map(
              "class"      -> error.getClass.getCanonicalName,
              "message"    -> error.getMessage,
              "stacktrace" -> error.getStackTrace.mkString("\n")
            ).asJava
          ).asJava
        )
        assertEquals(marker(1).toString, errorMarker.toString)
      }
    }

    loggingFixture.test(s"$name should log the error in the context even when logging a message") { f =>
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")

      for {
        _ <- logWithMsgFn(f.logger, error, "message", ctx)
        marker = flattenMarkers(f.appender.events.head.getMarker)
      } yield {
        val errorMarker = Markers.appendEntries(
          Map(
            "exception" -> Map(
              "class"      -> error.getClass.getCanonicalName,
              "message"    -> error.getMessage,
              "stacktrace" -> error.getStackTrace.mkString("\n")
            ).asJava
          ).asJava
        )
        assertEquals(marker(1).toString, errorMarker.toString)
      }
    }

    loggingFixture.test(s"$name should log the correct message even when logging error") { f =>
      val ctx   = LoggingContext("trid")
      val error = new Exception("error message")

      for {
        _ <- logWithMsgFn(f.logger, error, "message", ctx)
      } yield assertEquals(f.appender.events.head.getMessage, "message")
    }
  }
}
