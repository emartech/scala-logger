package com.emarsys.logger

import cats.data.ReaderT
import com.emarsys.logger.internal.{LoggableEncoded, LoggingContextMagnet}
import com.emarsys.logger.levels.LogLevel
import com.emarsys.logger.syntax._

object log {

  @deprecated("Do not use log[F].info, use log.info[F] instead", since = "0.8.0")
  def apply[F[_]](implicit logging: Logging[F]): Logging[F] = logging

  def debug[F[_]: Logging](msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].debug(msg)

  def debug[F[_]: Logging](msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].debug(msg, ctx)

  def info[F[_]: Logging](msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].info(msg)

  def info[F[_]: Logging](msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].info(msg, ctx)

  def warn[F[_]: Logging](msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].warn(msg)

  def warn[F[_]: Logging](msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].warn(msg, ctx)

  def warn[F[_]: Logging](cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].warn(cause)

  def warn[F[_]: Logging](cause: Throwable, ctx: LoggingContext): F[Unit] =
    Logging[F].warn(cause, ctx)

  def warn[F[_]: Logging](cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].warn(cause, msg)

  def warn[F[_]: Logging](cause: Throwable, msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].warn(cause, msg, ctx)

  def error[F[_]: Logging](msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].error(msg)

  def error[F[_]: Logging](msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].error(msg, ctx)

  def error[F[_]: Logging](cause: Throwable)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].error(cause)

  def error[F[_]: Logging](cause: Throwable, ctx: LoggingContext): F[Unit] =
    Logging[F].error(cause, ctx)

  def error[F[_]: Logging](cause: Throwable, msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[Unit] =
    Logging[F].error(cause, msg)

  def error[F[_]: Logging](cause: Throwable, msg: => String, ctx: LoggingContext): F[Unit] =
    Logging[F].error(cause, msg, ctx)

  def log[F[_]: Logging](level: LogLevel, msg: String, ctx: LoggingContext): F[Unit] =
    Logging[F].log(level, msg, ctx)

  def getContext[F[_]: Context]: F[LoggingContext] = Context[F].ask

  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def modifyContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(fa)(ctxExtender)

  def setContext[F[_]: Context, A](newContext: LoggingContext)(fa: => F[A]): F[A] =
    modifyContext(_ => newContext)(fa)

  def extendContext[F[_]: Context, A](params: (String, LoggableEncoded.Type)*)(fa: => F[A]): F[A] =
    modifyContext(_.addParameters(params: _*))(fa)

  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

}
