package com.emarsys.logger.ce

import cats.Monad
import cats.effect.IO
import ch.qos.logback.classic.Level
import com.emarsys.logger.{Logging, LoggingBehavior}
import munit.CatsEffectSuite

class CatsEffectLoggingSpec extends CatsEffectSuite with LoggingBehavior[IO] {
  implicit override val monadF: Monad[IO] = IO.asyncForIO

  override def createLogger(name: String): Logging[IO] =
    CatsEffectLogging.createEffectLoggerG[IO, IO](name).unsafeRunSync()

  simpleLog("CatsEffectLogging.debug", Level.DEBUG, { case (logger, msg, ctx) => logger.debug(msg)(ctx) })

  simpleLog("CatsEffectLogging.info", Level.INFO, { case (logger, msg, ctx) => logger.info(msg)(ctx) })

  simpleLog("CatsEffectLogging.warn", Level.WARN, { case (logger, msg, ctx) => logger.warn(msg)(ctx) })

  errorLog(
    "CatsEffectLogging.warn",
    { case (logger, error, ctx) => logger.warn(error)(ctx) },
    { case (logger, error, message, ctx) => logger.warn(error, message)(ctx) }
  )

  simpleLog("CatsEffectLogging.error", Level.ERROR, { case (logger, msg, ctx) => logger.error(msg)(ctx) })

  errorLog(
    "CatsEffectLogging.error",
    { case (logger, error, ctx) => logger.error(error)(ctx) },
    { case (logger, error, message, ctx) => logger.error(error, message)(ctx) }
  )

  test("CatsEffectLogging should compile with IO") {
    assertEquals(
      compileErrors("""
      import cats.effect.IO
      import com.emarsys.logger._

      implicit val logger: Logging[IO] = CatsEffectLogging.createEffectLogger[IO]("default").unsafeRunSync()
      implicit val lc: LoggingContext = LoggingContext("")

      log.warn("oh noe")
      """),
      ""
    )
  }

  test("CatsEffectLogging should compile with Logged[IO]") {
    assertEquals(
      compileErrors("""
      import cats.effect.IO
      import com.emarsys.logger._
      import com.emarsys.logger.ce._

      implicit val logger: Logging[LoggedIO] = CatsEffectLogging.createEffectLoggerG[LoggedIO, IO]("default").unsafeRunSync()

      log.warn("oh noe")
      """),
      ""
    )
  }
}
