package com.emarsys.logger

import cats.Id
import cats.effect.Sync
import com.emarsys.logger.internal.LoggingContextMagnet
import com.emarsys.logger.levels.LogLevel
import com.emarsys.logger.loggable._
import com.emarsys.logger.unsafe.UnsafeLogstashLogging

trait Logging[F[_]] {
  def debug(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(log(LogLevel.DEBUG, msg, _))

  def info(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(log(LogLevel.INFO, msg, _))

  def warn(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(log(LogLevel.WARN, msg, _))

  def warn(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = warn(cause, "Warn")

  def error(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => log(LogLevel.ERROR, msg, errorContext(cause, ctx)))

  def warn(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => log(LogLevel.WARN, msg, errorContext(cause, ctx)))

  def error(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(log(LogLevel.ERROR, msg, _))

  def error(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = error(cause, "Error")

  def log(level: LogLevel, msg: String, ctx: LoggingContext): F[Unit]

  private def errorContext(t: Throwable, ctx: LoggingContext) =
    ctx <> "exception" -> errorData(t)

  private def errorData(t: Throwable): LoggableValue = {
    val map = Map[String, LoggableValue](
      "class"      -> LoggableString(t.getClass.getCanonicalName),
      "message"    -> LoggableString(t.getMessage),
      "stacktrace" -> LoggableString(t.getStackTrace.toSeq.mkString("\n"))
    )

    if (t.getCause != null) LoggableObject(map + ("cause" -> errorData(t.getCause)))
    else LoggableObject(map)
  }
}

object Logging {

  def create[F[_]](logFn: (LogLevel, String, LoggingContext) => F[Unit]): Logging[F] = new Logging[F] {
    override def log(level: LogLevel, msg: String, ctx: LoggingContext): F[Unit] = logFn(level, msg, ctx)
  }

  def createEffectLogger[F[_]: Sync](name: String): Logging[F] = new LogbackEffectLogging[F](name)

  def createUnsafeLogger(name: String): Logging[Id] = new UnsafeLogstashLogging(name)
}
