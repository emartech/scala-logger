package com.emarsys.logger

import cats.effect.Sync
import com.emarsys.logger.internal.LoggingContextUtil
import com.emarsys.logger.levels.LogLevel
import org.slf4j.LoggerFactory

class LogbackEffectLogging[F[_]: Sync] private[logger] (name: String) extends Logging[F] {

  private val logger = LoggerFactory.getLogger(name)

  override def log(level: LogLevel, msg: String, ctx: LoggingContext): F[Unit] = Sync[F].delay {
    lazy val marker = LoggingContextUtil.toMarker(ctx)
    level match {
      case LogLevel.INFO =>
        if (logger.isInfoEnabled()) logger.info(marker, msg)
      case LogLevel.DEBUG =>
        if (logger.isDebugEnabled()) logger.debug(marker, msg)
      case LogLevel.WARN =>
        if (logger.isWarnEnabled()) logger.warn(marker, msg)
      case LogLevel.ERROR =>
        if (logger.isErrorEnabled()) logger.error(marker, msg)

    }
  }
}
