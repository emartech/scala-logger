package com.emarsys.logger

import ch.qos.logback.classic.Logger
import com.emarsys.logger.syntax._
import com.emarsys.logger.testutil.TestAppender
import munit.FunSuite
import org.slf4j.LoggerFactory

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

final case class LoggingSyntaxFixture(appender: TestAppender)(implicit
    val context: LoggingContext,
    val logging: Logging[Future]
)

class LoggingSyntaxLogSuccessFailureSpec extends FunSuite {

  private def await[T](f: Future[T]) = Await.ready(f, 1.second)

  private val fixture = FunFixture[LoggingSyntaxFixture](
    setup = { _ =>
      val loggerName: String                      = UUID.randomUUID().toString.take(8)
      implicit val loggingContext: LoggingContext = LoggingContext(loggerName)
      val unsafeLogger                            = Logging.createUnsafeLogger(loggerName)

      implicit val logger: Logging[Future] =
        Logging.create[Future]((level, msg, ctx) => Future.successful(unsafeLogger.log(level, msg, ctx)))

      val appender = new TestAppender
      appender.start()

      val underlyingLogger = LoggerFactory.getLogger(loggerName).asInstanceOf[Logger]
      underlyingLogger.detachAndStopAllAppenders()
      underlyingLogger.addAppender(appender)

      LoggingSyntaxFixture(appender)
    },
    teardown = _ => ()
  )

  fixture.test("LoggingSyntax should logSuccess for completed future") { f =>
    import f._

    await {
      Future[Int](1)
        .logSuccess("logSuccess")
        .logFailure("logFailure")
    }
    assertEquals(appender.events.length, 1)
    assertEquals(appender.events.head.getMessage, "logSuccess")
  }

  fixture.test("LoggingSyntax should logSuccess with value for completed future") { f =>
    import f._

    await {
      Future[Int](1)
        .logSuccess(r => s"logSuccess $r")
        .logFailure(e => s"logFailure $e")
    }
    assertEquals(appender.events.length, 1)
    assertEquals(appender.events.head.getMessage, "logSuccess 1")
  }

  fixture.test("LoggingSyntax should logFailure for failed future") { f =>
    import f._

    await {
      Future
        .failed[Int](new RuntimeException("Fail!"))
        .logSuccess("logSuccess")
        .logFailure("logFailure")
    }
    assertEquals(appender.events.length, 1)
    assertEquals(appender.events.head.getMessage, "logFailure")
  }

  fixture.test("LoggingSyntax should logFailure with value for failed future") { f =>
    import f._

    await {
      Future
        .failed[Int](new RuntimeException("Fail!"))
        .logSuccess(r => s"logSuccess $r")
        .logFailure(e => s"logFailure $e")
    }
    assertEquals(appender.events.length, 1)
    assertEquals(appender.events.head.getMessage, "logFailure java.lang.RuntimeException: Fail!")
  }

}
