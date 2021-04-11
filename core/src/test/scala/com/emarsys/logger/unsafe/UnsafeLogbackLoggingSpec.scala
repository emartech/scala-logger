package com.emarsys.logger.unsafe

import cats.{Id, Monad}
import ch.qos.logback.classic.Level
import com.emarsys.logger.{Logging, LoggingBehavior}
import munit.FunSuite

class UnsafeLogbackLoggingSpec extends FunSuite with LoggingBehavior[Id] {
  override implicit val monadF: Monad[Id] = cats.catsInstancesForId

  override def createLogger(name: String): Logging[Id] = Logging.createUnsafeLogger(name)

  simpleLog("Logging.debug", Level.DEBUG, { case (logger, msg, ctx) => logger.debug(msg)(ctx) })

  simpleLog("Logging.info", Level.INFO, { case (logger, msg, ctx) => logger.info(msg)(ctx) })

  simpleLog("Logging.warn", Level.WARN, { case (logger, msg, ctx) => logger.warn(msg)(ctx) })

  errorLog(
    "Logging.warn",
    { case (logger, error, ctx) => logger.warn(error)(ctx) },
    { case (logger, error, message, ctx) => logger.warn(error, message)(ctx) }
  )

  simpleLog("Logging.error", Level.ERROR, { case (logger, msg, ctx) => logger.error(msg)(ctx) })

  errorLog(
    "Logging.error",
    { case (logger, error, ctx) => logger.error(error)(ctx) },
    { case (logger, error, message, ctx) => logger.error(error, message)(ctx) }
  )
}
