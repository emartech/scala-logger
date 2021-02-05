package com.emarsys.logger

import cats.data.ReaderT
import cats.{Applicative, MonadError}
import com.emarsys.logger.internal.{LoggingContextMagnet, VarArgLoggableEncoder}
import com.emarsys.logger.loggable.{LoggableObject, LoggableValue}
import cats.syntax.applicativeError._
import cats.syntax.flatMap._

import scala.language.implicitConversions

trait LoggerSyntax extends VarArgLoggableEncoder {

  def log[F[_]](implicit logging: Logging[F]): Logging[F] = logging

  implicit final def toLoggingOps[F[_], A](fa: F[A]): LoggingOps[F, A] = new LoggingOps(fa)

  implicit final def toLoggedOps[F[_], A](fa: F[A]): LoggedOps[F, A] = new LoggedOps(fa)

  implicit class LoggingContextOps(lc: LoggingContext) {

    def addParameters(params: (String, HasLoggableEncoder)*): LoggingContext = {
      val extendedLogData = params.asInstanceOf[Seq[(String, LoggableValue)]].foldLeft(lc.logData.obj) {
        case (data, (key, value)) => data + ((key, value))
      }
      lc.copy(logData = LoggableObject(extendedLogData))
    }
  }

  implicit class ContextExtensionOps[F[_], A](fa: F[A]) {

    def withExtendedContext(params: (String, HasLoggableEncoder)*)(implicit context: Context[F]): F[A] =
      extendContext(params: _*)(fa)
  }

  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def getReaderContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

  def getContext[F[_]: Context]: F[LoggingContext] = Context[F].ask

  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

  def modifyContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(fa)(ctxExtender)

  def extendContext[F[_]: Context, A](params: (String, HasLoggableEncoder)*)(fa: => F[A]): F[A] =
    modifyContext(_.addParameters(params: _*))(fa)
}

final class LoggingOps[F[_], A](val fa: F[A]) extends AnyVal {

  def logFailure(implicit logging: Logging[F], me: MonadError[F, Throwable], ctx: LoggingContextMagnet[F]): F[A] =
    fa.onError { case error: Throwable =>
      logging.error(error)
    }

  def logFailure(
      msg: => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa.onError {
    case error: Throwable =>
      logging.error(error, msg)
  }

  def logFailure(
      createMsg: Throwable => String
  )(implicit logging: Logging[F], me: MonadError[F, Throwable], magnet: LoggingContextMagnet[F]): F[A] = fa.onError {
    case error: Throwable =>
      logging.error(error, createMsg(error))
  }

  def logFailure(createMsg: Throwable => String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa.onError { case error: Throwable =>
      magnet { ctx =>
        logging.error(error, createMsg(error))(ctxExtender(error, ctx))
      }
    }

  def logFailure(msg: String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(implicit
      logging: Logging[F],
      me: MonadError[F, Throwable],
      magnet: LoggingContextMagnet[F]
  ): F[A] =
    fa.onError { case error: Throwable =>
      magnet { ctx =>
        logging.error(error, msg)(ctxExtender(error, ctx))
      }
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

final class LoggedOps[F[_], A](val fa: F[A]) extends AnyVal {
  def toLogged: Logged[F, A] = ReaderT(_ => fa)
}
