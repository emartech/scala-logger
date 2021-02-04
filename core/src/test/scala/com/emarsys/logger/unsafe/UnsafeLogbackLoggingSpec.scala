package com.emarsys.logger.unsafe

import cats.Id
import ch.qos.logback.classic.Level
import com.emarsys.logger.{Logging, LoggingBehavior}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.flatspec.AnyFlatSpec

class UnsafeLogbackLoggingSpec extends AnyFlatSpec with Matchers with TypeCheckedTripleEquals with LoggingBehavior[Id] {

  override def runF(f: Id[Unit]): Unit = f

  override def createLogger(name: String): Logging[Id] = Logging.createUnsafeLogger(name)

  "Logging.debug" should behave like simpleLog(Level.DEBUG, { case (logger, msg, ctx) => logger.debug(msg)(ctx) })

  "Logging.info" should behave like simpleLog(Level.INFO, { case (logger, msg, ctx) => logger.info(msg)(ctx) })

  "Logging.warn" should behave like simpleLog(Level.WARN, { case (logger, msg, ctx) => logger.warn(msg)(ctx) })

  it should behave like errorLog(
    { case (logger, error, ctx) => logger.warn(error)(ctx) },
    { case (logger, error, message, ctx) => logger.warn(error, message)(ctx) }
  )

  "Logging.error" should behave like simpleLog(Level.ERROR, { case (logger, msg, ctx) => logger.error(msg)(ctx) })

  it should behave like errorLog(
    { case (logger, error, ctx) => logger.error(error)(ctx) },
    { case (logger, error, message, ctx) => logger.error(error, message)(ctx) }
  )
}
