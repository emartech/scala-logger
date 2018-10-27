package com.emarsys.logger
import cats.data.ReaderT
import cats.{Applicative, MonadError}
import com.emarsys.logger.internal.{LoggingContextMagnet, VarArgLoggableEncoder}
import com.emarsys.logger.loggable.{LoggableObject, LoggableValue}
import cats.syntax.applicativeError._
import cats.syntax.apply._
import cats.syntax.flatMap._

import scala.language.implicitConversions

trait LoggerSyntax extends VarArgLoggableEncoder {

  def log[F[_]](implicit logging: Logging[F]): Logging[F] = logging

  implicit final def toLoggingContextOps(lc: LoggingContext): LoggingContextOps = new LoggingContextOps(lc)

  implicit final def toLoggingOps[F[_], A](fa: F[A]): LoggingOps[F, A] = new LoggingOps(fa)

  implicit final def toLoggedOps[F[_], A](fa: F[A]): LoggedOps[F, A] = new LoggedOps(fa)

  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def getReaderContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

  def getContext[F[_]: Context]: F[LoggingContext] = Context[F].ask

  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

  def modifyContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(ctxExtender)(fa)

  def extendContext[F[_]: Context, A](params: (String, LoggingContextOpsImpl.HasLoggableEncoder)*)(fa: => F[A]): F[A] =
    modifyContext(_.addParameters(params: _*))(fa)
}

final class LoggingOps[F[_], A](val fa: F[A]) extends AnyVal {
  def logFailure(implicit logging: Logging[F], me: MonadError[F, Throwable], ctx: LoggingContextMagnet[F]): F[A] =
    fa onError {
      case error: Throwable =>
        logging.error(error)
    }

  def logFailure(
      msg: => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa onError {
    case error: Throwable =>
      logging.error(error, msg)
  }

  def logFailure(
      createMsg: Throwable => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa onError {
    case error: Throwable =>
      logging.error(error, createMsg(error))
  }

  def logFailure(createMsg: Throwable => String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(
      implicit logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa onError {
      case error: Throwable =>
        magnet { ctx =>
          logging.error(error, createMsg(error))(ctxExtender(error, ctx))
        }
    }

  def logFailure(msg: String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(
      implicit logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa onError {
      case error: Throwable =>
        magnet { ctx =>
          logging.error(error, msg)(ctxExtender(error, ctx))
        }
    }

  def logSuccess(
      msg: String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] =
    fa <* logging.info(msg)

  def logSuccess(
      createMsg: A => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa flatTap {
    value =>
      logging.info(createMsg(value))
  }

  def logSuccess(createMsg: A => String, ctxExtender: (A, LoggingContext) => LoggingContext)(
      implicit logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] = fa flatTap { value =>
    magnet { ctx =>
      logging.info(createMsg(value))(ctxExtender(value, ctx))
    }
  }

  def logSuccess(msg: String, ctxExtender: (A, LoggingContext) => LoggingContext)(
      implicit logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa flatTap { value =>
      magnet { ctx =>
        logging.info(msg)(ctxExtender(value, ctx))
      }
    }

  def withModifiedContext(ctxExtender: LoggingContext => LoggingContext)(implicit context: Context[F]): F[A] =
    Context[F].local(ctxExtender)(fa)

  def withExtendedContext(
      params: (String, LoggingContextOpsImpl.HasLoggableEncoder)*
  )(implicit context: Context[F]): F[A] =
    withModifiedContext(LoggingContextOpsImpl.addParameters(_)(params: _*))

}

final class LoggingContextOps(val lc: LoggingContext) extends AnyVal {
  def addParameters(params: (String, LoggingContextOpsImpl.HasLoggableEncoder)*): LoggingContext =
    LoggingContextOpsImpl.addParameters(lc)(params: _*)
}

private object LoggingContextOpsImpl extends VarArgLoggableEncoder {
  def addParameters(lc: LoggingContext)(params: (String, HasLoggableEncoder)*): LoggingContext = {
    val extendedLogData = params.asInstanceOf[Seq[(String, LoggableValue)]].foldLeft(lc.logData.obj) {
      case (data, (key, value)) => data + ((key, value))
    }
    lc.copy(logData = LoggableObject(extendedLogData))
  }
}

final class LoggedOps[F[_], A](val fa: F[A]) extends AnyVal {
  def toLogged: Logged[F, A] = ReaderT(_ => fa)
}
