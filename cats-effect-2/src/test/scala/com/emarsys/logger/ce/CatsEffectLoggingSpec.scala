package com.emarsys.logger.ce

import cats.effect.IO
import ch.qos.logback.classic.Level
import com.emarsys.logger.{Logging, LoggingBehavior}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class CatsEffectLoggingSpec extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals with LoggingBehavior[IO] {

  override def runF(f: IO[Unit]): Unit = f.unsafeRunSync()

  override def createLogger(name: String): Logging[IO] =
    CatsEffectLogging.createEffectLoggerG[IO, IO](name).unsafeRunSync()

  "CatsEffectLogging.debug" should behave like simpleLog(
    Level.DEBUG,
    { case (logger, msg, ctx) => logger.debug(msg)(ctx) }
  )

  "CatsEffectLogging.info" should behave like simpleLog(
    Level.INFO,
    { case (logger, msg, ctx) => logger.info(msg)(ctx) }
  )

  "CatsEffectLogging.warn" should behave like simpleLog(
    Level.WARN,
    { case (logger, msg, ctx) => logger.warn(msg)(ctx) }
  )

  it should behave like errorLog(
    { case (logger, error, ctx) => logger.warn(error)(ctx) },
    { case (logger, error, message, ctx) => logger.warn(error, message)(ctx) }
  )

  "CatsEffectLogging.error" should behave like simpleLog(
    Level.ERROR,
    { case (logger, msg, ctx) => logger.error(msg)(ctx) }
  )

  it should behave like errorLog(
    { case (logger, error, ctx) => logger.error(error)(ctx) },
    { case (logger, error, message, ctx) => logger.error(error, message)(ctx) }
  )

  "CatsEffectLogging" should "compile with IO" in {
    """
      |import cats.effect.IO
      |import com.emarsys.logger._
      |import com.emarsys.logger.syntax._
      |
      |implicit val logger: Logging[IO] = CatsEffectLogging.createEffectLogger[IO]("default").unsafeRunSync()
      |implicit val lc: LoggingContext = LoggingContext("")
      |
      |log[IO].warn("oh noe")
      """.stripMargin should compile
  }

  it should "compile with Logged[IO]" in {
    """
      |import cats.effect.IO
      |import com.emarsys.logger._
      |import com.emarsys.logger.syntax._
      |
      |type LoggedIO[A] = Logged[IO, A]
      |
      |implicit val logger: Logging[LoggedIO] = CatsEffectLogging.createEffectLoggerG[LoggedIO, IO]("default").unsafeRunSync()
      |
      |log[LoggedIO].warn("oh noe")
      """.stripMargin should compile
  }

}
