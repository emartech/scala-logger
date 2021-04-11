package com.emarsys.logger.ce

import cats.data.Chain
import cats.effect.std.CountDownLatch
import cats.effect.{IO, Ref}
import com.emarsys.logger.levels.LogLevel
import com.emarsys.logger.{log, Context, Logging, LoggingContext}
import munit.CatsEffectSuite

class CatsEffectFiberLocalContextSpec extends CatsEffectSuite {
  import com.emarsys.logger.syntax.toContextExtensionOps

  val initialLoggingContext: LoggingContext = LoggingContext("hello")

  case class Call(level: LogLevel, message: String, loggingContext: LoggingContext)

  def testLogScope[A](f: Logging[IO] => Context[IO] => IO[A]): IO[Chain[Call]] =
    for {
      ref <- Ref.of[IO, Chain[Call]](Chain.empty)
      logging = Logging.create((level, msg, ctx) => ref.update(_ :+ Call(level, msg, ctx)))
      context <- CatsEffectLogging.createIOLocalContext(initialLoggingContext)
      _       <- f(logging)(context)
      result  <- ref.get
    } yield result

  implicit final class ChainAssertOps(calls: Chain[Call]) {

    def assertContextAt(message: String, expectedContext: LoggingContext): Unit = {
      val call = calls.find(_.message == message)
      call match {
        case Some(value) => assert(value.loggingContext == expectedContext)
        case None        => fail(s"expected call with message `$message` but not found")
      }
    }
  }

  test("CatsEffectLoggingSpec#context should propagate LoggingContext to Logging") {

    for {
      calls <- testLogScope { implicit logging => implicit context =>
        log.info("log1")
      }
    } yield calls.assertContextAt("log1", initialLoggingContext)
  }

  test("CatsEffectLoggingSpec#context should add to context") {
    for {
      calls <- testLogScope { implicit logging => implicit context =>
        log.info("log1").withExtendedContext("a" -> 1)
      }
    } yield calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
  }

  test("CatsEffectLoggingSpec#context should clean context after scope") {
    for {
      calls <- testLogScope { implicit logging => implicit context =>
        log.info("log1").withExtendedContext("a" -> 1) *>
          log.info("log2")
      }
    } yield {
      calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
      calls.assertContextAt("log2", initialLoggingContext)
    }
  }

  test("CatsEffectLoggingSpec#context should propagate context to child fibers") {
    for {
      calls <- testLogScope { implicit logging => implicit context =>
        log.info("log1").start.withExtendedContext("a" -> 1).flatMap(_.join)
      }
    } yield calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
  }

  test("CatsEffectLoggingSpec#context should should ensure fibers do not affect their parent's context") {
    for {
      calls <- testLogScope { implicit logging => implicit context =>
        for {
          latch1 <- CountDownLatch[IO](1)
          latch2 <- CountDownLatch[IO](1)
          fiber  <- (latch2.release *> log.info("log1") *> latch1.await).withExtendedContext("a" -> 1).start
          _      <- latch2.await
          _      <- log.info("log2")
          _      <- latch1.release
          _      <- fiber.join
        } yield ()
      }
    } yield calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
  }

}
