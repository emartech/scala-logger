package com.emarsys.logger

import cats.Applicative
import net.logstash.logback.marker.LogstashMarker
import org.slf4j.{Logger, LoggerFactory}

trait Logging[F[_]] {
  def debug(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logDebug(msg, _))
  protected def logDebug(msg: => String, ctx: LoggingContext): F[Unit]

  def info(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logInfo(msg, _))
  protected def logInfo(msg: => String, ctx: LoggingContext): F[Unit]

  def warn(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logWarn(msg, _))
  def warn(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = error(cause, "Warn")
  def warn(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => logWarn(msg, errorContext(cause, ctx)))
  protected def logWarn(msg: => String, ctx: LoggingContext): F[Unit]

  def error(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(logError(msg, _))
  def error(cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] = error(cause, "Error")
  def error(cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    magnet(ctx => logError(msg, errorContext(cause, ctx)))
  protected def logError(msg: => String, ctx: LoggingContext): F[Unit]

  private def errorContext(t: Throwable, ctx: LoggingContext) =
    ctx + ("exception" -> errorData(t))

  private def errorData(t: Throwable): Map[String, _] = {
    val map = Map(
      "class"      -> t.getClass,
      "message"    -> t.getMessage,
      "stacktrace" -> t.getStackTrace.toSeq.map(_.toString)
    )

    if (t.getCause != null) map + ("cause" -> errorData(t.getCause))
    else map
  }
}

object Logging extends LowPriorityLogstashLogging

trait LowPriorityLogstashLogging {
  private lazy val logger: Logger = LoggerFactory.getLogger("default")

  implicit def defaultLogging[F[_]: Applicative]: Logging[F] = new Logging[F] {
    import cats.syntax.applicative._
    import net.logstash.logback.marker.Markers._

    import scala.collection.JavaConverters._

    def logDebug(msg: => String, ctx: LoggingContext): F[Unit] = {
      if (logger.isDebugEnabled()) logger.debug(context(ctx), msg)
    }.pure

    def logInfo(msg: => String, ctx: LoggingContext): F[Unit] = {
      if (logger.isInfoEnabled) logger.info(context(ctx), msg)
    }.pure

    def logWarn(msg: => String, ctx: LoggingContext): F[Unit] = {
      if (logger.isWarnEnabled) logger.warn(context(ctx), msg)
    }.pure

    def logError(msg: => String, ctx: LoggingContext): F[Unit] = {
      if (logger.isErrorEnabled) logger.error(context(ctx), msg)
    }.pure

    private def context(ctx: LoggingContext): LogstashMarker = {
      append("transactionId", ctx.transactionID) and appendEntries(toJava(ctx.logData))
    }

    private def toJava(logData: Map[String, Any]): java.util.Map[_, _] = logData.mapValues(toJava).asJava
    private def toJava(a: Any): Any = a match {
      case m: Map[_, _]   => m.mapValues(toJava).asJava
      case i: Iterable[_] => i.map(toJava).asJava
      case _              => a
    }
  }
}
