package com.emarsys.logger

import cats.effect.IO
import ch.qos.logback.classic.Level
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{FlatSpec, Matchers}

class LogbackEffectLoggingSpec extends FlatSpec with Matchers with TypeCheckedTripleEquals with LoggingBehavior[IO] {

  override def runF(f: IO[Unit]): Unit = f.unsafeRunSync()

  override def createLogger(name: String): Logging[IO] = Logging.createEffectLogger(name)

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
