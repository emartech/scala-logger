package com.emarsys.logger

import cats.Applicative
import cats.effect.{IO, IOLocal, Sync}
import cats.mtl.Local
import com.emarsys.logger.internal.LoggingContextUtil
import com.emarsys.logger.levels.LogLevel
import org.slf4j.LoggerFactory

class CatsEffectLogging[F[_]: Sync] private[logger] (name: String) extends Logging[F] {
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

object CatsEffectLogging {
  def createEffectLogger[F[_]: Sync](name: String): F[Logging[F]] = createEffectLoggerG[F, F](name)

  def createEffectLoggerG[F[_]: Sync, G[_]: Sync](name: String): G[Logging[F]] =
    Sync[G].delay(new CatsEffectLogging[F](name))

  def fiberLocalContext(initialLoggingContext: LoggingContext): IO[Context[IO]] =
    IOLocal[LoggingContext](initialLoggingContext).map { localLoggingContext =>
      new Local[IO, LoggingContext] {
        override def applicative: Applicative[IO] = implicitly

        override def ask[E2 >: LoggingContext]: IO[E2] = localLoggingContext.get

        override def local[A](fa: IO[A])(f: LoggingContext => LoggingContext): IO[A] = for {
          originalLoggingContext <- localLoggingContext.get
          result                 <- (localLoggingContext.set(f(originalLoggingContext)) *> fa)
            .guarantee(localLoggingContext.set(originalLoggingContext))
        } yield result
      }
    }
}
