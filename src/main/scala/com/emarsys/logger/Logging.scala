package com.emarsys.logger

import cats.effect.Sync
import cats.{Applicative, Id}
import com.emarsys.logger.internal.{LoggingContextMagnet, LoggingContextUtil}
import com.emarsys.logger.levels.LogLevel
import com.emarsys.logger.loggable._
import net.logstash.logback.marker.LogstashMarker
import org.slf4j.{Logger, LoggerFactory}

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

object Logging extends ApplicativeLogging {

  def create[F[_]](logFn: (LogLevel, String, LoggingContext) => F[Unit]): Logging[F] = new Logging[F] {
    override def log(level: LogLevel, msg: String, ctx: LoggingContext): F[Unit] = logFn(level, msg, ctx)
  }

  implicit def defaultLogging[F[_]: Sync](implicit ul: Logging[Id]): Logging[F] = create[F] { (level, msg, ctx) =>
    Sync[F].delay(ul.log(level, msg, ctx))
  }
}

trait UnsafeLogstashLogging {

  implicit lazy val unsafeLogstashLogging: Logging[Id] = {
    lazy val logger: Logger = LoggerFactory.getLogger("default")
    Logging.create[Id] {
      case (LogLevel.DEBUG, msg, ctx) =>
        if (logger.isDebugEnabled()) logger.debug(LoggingContextUtil.toMarker(ctx), msg)
      case (LogLevel.INFO, msg, ctx) =>
        if (logger.isInfoEnabled()) logger.info(LoggingContextUtil.toMarker(ctx), msg)
      case (LogLevel.WARN, msg, ctx) =>
        if (logger.isWarnEnabled()) logger.warn(LoggingContextUtil.toMarker(ctx), msg)
      case (LogLevel.ERROR, msg, ctx) =>
        if (logger.isErrorEnabled()) logger.error(LoggingContextUtil.toMarker(ctx), msg)
    }
  }
}

trait ApplicativeLogging extends UnsafeLogstashLogging {
  import cats.syntax.applicative._

  implicit def defaultApplicativeLogging[F[_]: Applicative](implicit ul: Logging[Id]): Logging[F] =
    Logging.create[F] { (level, msg, ctx) =>
      ul.log(level, msg, ctx).pure
    }
}
