package com.emarsys.logger

import cats.data.Chain
import cats.effect.std.CountDownLatch
import cats.effect.{IO, Ref}
import com.emarsys.logger.levels.LogLevel
import org.scalatest.compatible.Assertion
import org.scalatest.wordspec.AnyWordSpecLike

class CatsEffectFiberLocalContextSpec extends AnyWordSpecLike {
  import cats.effect.unsafe.implicits.global
  import com.emarsys.logger.syntax._

  val initialLoggingContext: LoggingContext = LoggingContext("hello")

  case class Call(level: LogLevel, message: String, loggingContext: LoggingContext)

  def testLogScope(f: Logging[IO] => Context[IO] => IO[_]): Chain[Call] = {
    val io = for {
      ref <- Ref.of[IO, Chain[Call]](Chain.empty)
      logging = Logging.create((level, msg, ctx) => ref.update(_ :+ Call(level, msg, ctx)))
      context <- CatsEffectLogging.fiberLocalContext(initialLoggingContext)
      _       <- f(logging)(context)
      result  <- ref.get
    } yield result
    io.unsafeRunSync()
  }

  implicit final class ChainAssertOps(calls: Chain[Call]) {

    def assertContextAt(message: String, expectedContext: LoggingContext): Assertion = {
      val call = calls.find(_.message == message)
      call match {
        case Some(value) => assert(value.loggingContext == expectedContext)
        case None        => fail(s"expected call with message `$message` but not found")
      }
    }
  }

  "CatsEffectLoggingSpec#context" should {
    "propagate LoggingContext to Logging" in {
      val calls = testLogScope { implicit logging => implicit context =>
        log.info("log1")
      }

      calls.assertContextAt("log1", initialLoggingContext)
    }

    "add to context" in {
      val calls = testLogScope { implicit logging => implicit context =>
        log.info("log1").withExtendedContext("a" -> 1)
      }

      calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
    }

    "clean context after scope" in {
      val calls = testLogScope { implicit logging => implicit context =>
        log.info("log1").withExtendedContext("a" -> 1) *>
          log.info("log2")
      }

      calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
      calls.assertContextAt("log2", initialLoggingContext)
    }

    "propagate context to child fibers" in {
      val calls = testLogScope { implicit logging => implicit context =>
        log.info("log1").start.withExtendedContext("a" -> 1).flatMap(_.join)
      }

      calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
    }

    "should ensure fibers do not affect their parent's context" in {
      val calls = testLogScope { implicit logging => implicit context =>
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

      calls.assertContextAt("log1", initialLoggingContext.addParameter("a" -> 1))
    }

  }

}
