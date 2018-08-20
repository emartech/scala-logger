package com.emarsys.logger

import cats.effect.Sync
import cats.{Applicative, Id}
import com.emarsys.logger.internal.LoggingContextMagnet
import com.emarsys.logger.loggable._
import net.logstash.logback.marker.LogstashMarker
import org.slf4j.{Logger, LoggerFactory}

trait Logging[F[_]] {
  def debug(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logDebug(msg, _))

  def info(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logInfo(msg, _))

  def warn(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logWarn(msg, _))

  def warn(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = error(cause, "Warn")

  def error(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => logError(msg, errorContext(cause, ctx)))

  def warn(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => logWarn(msg, errorContext(cause, ctx)))

  def error(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logError(msg, _))

  def error(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = error(cause, "Error")

  private[logger] def logDebug(msg: => String, ctx: LoggingContext): F[Unit]

  private[logger] def logInfo(msg: => String, ctx: LoggingContext): F[Unit]

  private[logger] def logWarn(msg: => String, ctx: LoggingContext): F[Unit]

  private[logger] def logError(msg: => String, ctx: LoggingContext): F[Unit]

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
  implicit def defaultLogging[F[_]: Sync](implicit ul: Logging[Id]): Logging[F] = new Logging[F] {
    def logDebug(msg: => String, ctx: LoggingContext): F[Unit] = Sync[F].delay {
      ul.logDebug(msg, ctx)
    }

    def logInfo(msg: => String, ctx: LoggingContext): F[Unit] = Sync[F].delay {
      ul.logInfo(msg, ctx)
    }

    def logWarn(msg: => String, ctx: LoggingContext): F[Unit] = Sync[F].delay {
      ul.logWarn(msg, ctx)
    }

    def logError(msg: => String, ctx: LoggingContext): F[Unit] = Sync[F].delay {
      ul.logError(msg, ctx)
    }
  }
}

trait UnsafeLogstashLogging {

  import net.logstash.logback.marker.Markers._

  import scala.collection.JavaConverters._

  private def context(ctx: LoggingContext): LogstashMarker = {
    append("transactionId", ctx.transactionID) and appendEntries(toJava(ctx.logData.obj))
  }

  private def toJava(logData: Map[String, LoggableValue]): java.util.Map[_, _] = logData.mapValues(toJava).asJava
  private def toJava(lv: LoggableValue): Any = lv match {
    case LoggableIntegral(value) => value
    case LoggableFloating(value) => value
    case LoggableString(value)   => value
    case LoggableBoolean(value)  => value
    case LoggableList(list)      => list.map(toJava).asJava
    case LoggableObject(obj)     => obj.mapValues(toJava).asJava
    case LoggableNil             => null
  }

  implicit lazy val unsafeLogstashLogging: Logging[Id] = new Logging[Id] {
    private lazy val logger: Logger = LoggerFactory.getLogger("default")

    def logDebug(msg: => String, ctx: LoggingContext): Unit = {
      if (logger.isDebugEnabled()) logger.debug(context(ctx), msg)
    }

    def logInfo(msg: => String, ctx: LoggingContext): Unit = {
      if (logger.isInfoEnabled) logger.info(context(ctx), msg)
    }

    def logWarn(msg: => String, ctx: LoggingContext): Unit = {
      if (logger.isWarnEnabled) logger.warn(context(ctx), msg)
    }

    def logError(msg: => String, ctx: LoggingContext): Unit = {
      if (logger.isErrorEnabled) logger.error(context(ctx), msg)
    }
  }

}

trait ApplicativeLogging extends UnsafeLogstashLogging {
  import cats.syntax.applicative._

  implicit def defaultApplicativeLogging[F[_]: Applicative](implicit ul: Logging[Id]): Logging[F] = new Logging[F] {
    def logDebug(msg: => String, ctx: LoggingContext): F[Unit] = {
      ul.logDebug(msg, ctx)
    }.pure

    def logInfo(msg: => String, ctx: LoggingContext): F[Unit] = {
      ul.logInfo(msg, ctx)
    }.pure

    def logWarn(msg: => String, ctx: LoggingContext): F[Unit] = {
      ul.logWarn(msg, ctx)
    }.pure

    def logError(msg: => String, ctx: LoggingContext): F[Unit] = {
      ul.logError(msg, ctx)
    }.pure
  }
}
