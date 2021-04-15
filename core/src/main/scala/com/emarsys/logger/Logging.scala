package com.emarsys.logger

import cats.Id
import com.emarsys.logger.internal.LoggingContextMagnet
import com.emarsys.logger.levels.LogLevel
import com.emarsys.logger.loggable._
import com.emarsys.logger.unsafe.UnsafeLogstashLogging

trait Logging[F[_]] {

  def debug(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(debug(msg, _))

  def debug(msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.DEBUG, msg, ctx)

  def info(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(info(msg, _))

  def info(msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.INFO, msg, ctx)

  def warn(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(warn(msg, _))

  def warn(msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.WARN, msg, ctx)

  def warn(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(warn(cause, "Warn", _))

  def warn(cause: Throwable, ctx: LoggingContext): F[Unit] =
    warn(cause, "Warn", ctx)

  def warn(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => warn(cause, msg, ctx))

  def warn(cause: Throwable, msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.WARN, msg, errorContext(cause, ctx))

  def error(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(error(msg, _))

  def error(msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.ERROR, msg, ctx)

  def error(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(error(cause, "Error", _))

  def error(cause: Throwable, ctx: LoggingContext): F[Unit] =
    error(cause, "Error", ctx)

  def error(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => error(cause, msg, errorContext(cause, ctx)))

  def error(cause: Throwable, msg: => String, ctx: LoggingContext): F[Unit] =
    log(LogLevel.ERROR, msg, errorContext(cause, ctx))

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
  def apply[F[_]](implicit ev: Logging[F]): Logging[F] = ev

  def create[F[_]](logFn: (LogLevel, String, LoggingContext) => F[Unit]): Logging[F] =
    (level: LogLevel, msg: String, ctx: LoggingContext) => logFn(level, msg, ctx)

  def createUnsafeLogger(name: String): Logging[Id] = new UnsafeLogstashLogging(name)
}
