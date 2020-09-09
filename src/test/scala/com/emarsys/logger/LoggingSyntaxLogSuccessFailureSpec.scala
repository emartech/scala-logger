package com.emarsys.logger

import java.util.UUID

import ch.qos.logback.classic.Logger
import com.emarsys.logger.testutil.TestAppender
import com.emarsys.logger.syntax._
import org.slf4j.LoggerFactory
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class LoggingSyntaxLogSuccessFailureSpec extends AnyWordSpec with Matchers {
  "LoggingSyntax" should {
    def await[T](f: Future[T]) = Await.ready(f, 1.second)

    "logSuccess for completed future" in new LoggingScope {
      await {
        Future[Int](1)
          .logSuccess("logSuccess")
          .logFailure("logFailure")
      }
      appender.events.length shouldBe 1
      appender.events.head.getMessage shouldBe "logSuccess"
    }
    "logSuccess with value for completed future" in new LoggingScope {
      await {
        Future[Int](1)
          .logSuccess(r => s"logSuccess $r")
          .logFailure(e => s"logFailure $e")
      }
      appender.events.length shouldBe 1
      appender.events.head.getMessage shouldBe "logSuccess 1"
    }
    "logFailure for failed future" in new LoggingScope {
      await {
        Future
          .failed[Int](new RuntimeException("Fail!"))
          .logSuccess("logSuccess")
          .logFailure("logFailure")
      }
      appender.events.length shouldBe 1
      appender.events.head.getMessage shouldBe "logFailure"
    }
    "logFailure with value for failed future" in new LoggingScope {
      await {
        Future
          .failed[Int](new RuntimeException("Fail!"))
          .logSuccess(r => s"logSuccess $r")
          .logFailure(e => s"logFailure $e")
      }
      appender.events.length shouldBe 1
      appender.events.head.getMessage shouldBe "logFailure java.lang.RuntimeException: Fail!"
    }
  }

  trait LoggingScope {

    private val loggerName: String              = UUID.randomUUID().toString.take(8)
    implicit val loggingContext: LoggingContext = LoggingContext(loggerName)
    private val unsafeLogger                    = Logging.createUnsafeLogger(loggerName)

    implicit val logger: Logging[Future] =
      Logging.create[Future]((level, msg, ctx) => Future.successful(unsafeLogger.log(level, msg, ctx)))

    val appender = new TestAppender
    appender.start()

    private val underlyingLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
    underlyingLogger.detachAndStopAllAppenders()
    underlyingLogger.addAppender(appender)
  }
}
