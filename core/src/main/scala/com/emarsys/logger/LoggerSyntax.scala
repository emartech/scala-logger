package com.emarsys.logger

import cats.data.ReaderT
import cats.{Applicative, MonadError}
import com.emarsys.logger.internal.{LoggableEncoded, LoggingContextMagnet}
import com.emarsys.logger.loggable.LoggableObject
import com.emarsys.logger.{log => logo}

import scala.language.implicitConversions

trait LoggerSyntax {

  @deprecated("Use com.emarsys.logger.log object instead", since = "0.8.0")
  def log[F[_]](implicit logging: Logging[F]): Logging[F] = logging

  implicit final def toLoggingOps[F[_], A](fa: F[A]): LoggingOps[F, A] = new LoggingOps(fa)

  implicit final def toLoggedOps[F[_], A](fa: F[A]): LoggedOps[F, A] = new LoggedOps(fa)

  implicit final def toLoggingContextOps(lc: LoggingContext): LoggingContextOps = new LoggingContextOps(lc)

  implicit final def toContextExtensionOps[F[_], A](fa: F[A]): ContextExtensionOps[F, A] = new ContextExtensionOps(fa)

  @deprecated("Use com.emarsys.logger.log.withContext instead", since = "0.8.0")
  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = logo.withContext(block)

  @deprecated("Use com.emarsys.logger.log.getContext instead", since = "0.8.0")
  def getReaderContext[F[_]: Applicative]: Logged[F, LoggingContext] = logo.getContext[Logged[F, *]]

  @deprecated("Use com.emarsys.logger.log.getContext instead", since = "0.8.0")
  def getContext[F[_]: Context]: F[LoggingContext] = logo.getContext

  @deprecated("Use com.emarsys.logger.log.extendReaderContext instead", since = "0.8.0")
  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    logo.extendReaderContext(ctxExtender)(block)

  @deprecated("Use com.emarsys.logger.log.modifyContext instead", since = "0.8.0")
  def modifyContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    logo.modifyContext(ctxExtender)(fa)

  @deprecated("Use com.emarsys.logger.log.extendContext instead", since = "0.8.0")
  def extendContext[F[_]: Context, A](params: (String, LoggableEncoded.Type)*)(fa: => F[A]): F[A] =
    logo.extendContext(params: _*)(fa)
}

final class LoggingContextOps private[logger] (val lc: LoggingContext) extends AnyVal {

  def addParameters(params: (String, LoggableEncoded.Type)*): LoggingContext = {
    val extendedLogData = params.foldLeft(lc.logData.obj) { case (data, (key, value)) =>
      data + ((key, value))
    }
    lc.copy(logData = LoggableObject(extendedLogData))
  }
}

final class ContextExtensionOps[F[_], A] private[logger] (val fa: F[A]) extends AnyVal {

  def withExtendedContext(params: (String, LoggableEncoded.Type)*)(implicit context: Context[F]): F[A] =
    logo.extendContext(params: _*)(fa)
}

final class LoggingOps[F[_], A] private[logger] (val fa: F[A]) extends AnyVal {
  import cats.syntax.applicativeError._
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def logFailure(implicit logging: Logging[F], me: MonadError[F, Throwable], ctx: LoggingContextMagnet[F]): F[A] =
    fa.handleErrorWith(error => logging.error(error) *> error.raiseError)

  def logFailure(
      msg: => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] =
    fa.handleErrorWith(error => logging.error(error, msg) *> error.raiseError)

  def logFailure(
      createMsg: Throwable => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] =
    fa.handleErrorWith(error => logging.error(error, createMsg(error)) *> error.raiseError)

  def logFailure(createMsg: Throwable => String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa.handleErrorWith { error =>
      magnet { ctx =>
        logging.error(error, createMsg(error))(ctxExtender(error, ctx))
      } *> error.raiseError
    }

  def logFailure(msg: String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa.handleErrorWith { error =>
      magnet { ctx =>
        logging.error(error, msg)(ctxExtender(error, ctx))
      } *> error.raiseError
    }

  def logSuccess(
      msg: String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] =
    fa.flatTap(_ => logging.info(msg))

  def logSuccess(
      createMsg: A => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa.flatTap {
    value =>
      logging.info(createMsg(value))
  }

  def logSuccess(createMsg: A => String, ctxExtender: (A, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] = fa.flatTap { value =>
    magnet { ctx =>
      logging.info(createMsg(value))(ctxExtender(value, ctx))
    }
  }

  def logSuccess(msg: String, ctxExtender: (A, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa.flatTap { value =>
      magnet { ctx =>
        logging.info(msg)(ctxExtender(value, ctx))
      }
    }

  def withModifiedContext(ctxExtender: LoggingContext => LoggingContext)(implicit context: Context[F]): F[A] =
    Context[F].local(fa)(ctxExtender)
}

final class LoggedOps[F[_], A] private[logger] (val fa: F[A]) extends AnyVal {
  def toLogged: Logged[F, A] = ReaderT(_ => fa)
}
