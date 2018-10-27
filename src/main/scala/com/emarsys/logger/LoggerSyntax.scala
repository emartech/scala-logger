package com.emarsys.logger
import cats.data.ReaderT
import cats.{Applicative, Id, MonadError}
import com.emarsys.logger.internal.{LoggingContextMagnet, VarArgLoggableEncoder}
import com.emarsys.logger.loggable.{LoggableObject, LoggableValue}

trait LoggerSyntax extends VarArgLoggableEncoder {
  import cats.syntax.applicativeError._
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def log[F[_]](implicit logging: Logging[F]): Logging[F] = logging

  implicit class LoggingMonadErrorOps[F[_]: Logging: MonadError[?[_], Throwable], A](fa: F[A]) {
    def logFailure(implicit ctx: LoggingContextMagnet[F]): F[A] = fa onError {
      case error: Throwable =>
        log.error(error)
    }

    def logFailure(msg: => String)(implicit magnet: LoggingContextMagnet[F]): F[A] = fa onError {
      case error: Throwable =>
        log.error(error, msg)
    }

    def logFailure(createMsg: Throwable => String)(implicit magnet: LoggingContextMagnet[F]): F[A] = fa onError {
      case error: Throwable =>
        log.error(error, createMsg(error))
    }

    def logFailure(createMsg: Throwable => String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(
        implicit magnet: LoggingContextMagnet[F]
    ): F[A] =
      fa onError {
        case error: Throwable =>
          magnet { ctx =>
            log.error(error, createMsg(error))(ctxExtender(error, ctx))
          }
      }

    def logFailure(msg: String, ctxExtender: (Throwable, LoggingContext) => LoggingContext)(
        implicit magnet: LoggingContextMagnet[F]
    ): F[A] =
      fa onError {
        case error: Throwable =>
          magnet { ctx =>
            log.error(error, msg)(ctxExtender(error, ctx))
          }
      }

    def logSuccess(msg: String)(implicit magnet: LoggingContextMagnet[F]): F[A] = fa <* log.info(msg)

    def logSuccess(createMsg: A => String)(implicit magnet: LoggingContextMagnet[F]): F[A] = fa flatTap { value =>
      log.info(createMsg(value))
    }

    def logSuccess(createMsg: A => String, ctxExtender: (A, LoggingContext) => LoggingContext)(
        implicit magnet: LoggingContextMagnet[F]
    ): F[A] = fa flatTap { value =>
      magnet { ctx =>
        log.info(createMsg(value))(ctxExtender(value, ctx))
      }
    }

    def logSuccess(msg: String, ctxExtender: (A, LoggingContext) => LoggingContext)(
        implicit magnet: LoggingContextMagnet[F]
    ): F[A] =
      fa flatTap { value =>
        magnet { ctx =>
          log.info(msg)(ctxExtender(value, ctx))
        }
      }

  }

  implicit class ContextOps[F[_]: Context, A](fa: F[A]) {
    def withModifiedContext(ctxExtender: LoggingContext => LoggingContext): F[A] =
      modifyContext(ctxExtender)(fa)

    def withExtendedContext(params: (String, HasLoggableEncoder)*): F[A] =
      extendContext(params: _*)(fa)
  }

  implicit class LoggedOps[F[_], A](fa: F[A]) {
    def toLogged: Logged[F, A] = withContext(_ => fa)
  }

  implicit class LoggingContextOps(lc: LoggingContext) {
    def addParameters(params: (String, HasLoggableEncoder)*): LoggingContext = {
      val extendedLogData = params.asInstanceOf[Seq[(String, LoggableValue)]].foldLeft(lc.logData.obj) {
        case (data, (key, value)) => data + ((key, value))
      }
      lc.copy(logData = LoggableObject(extendedLogData))
    }
  }

  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def getReaderContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

  def getContext[F[_]: Context]: F[LoggingContext] = Context[F].ask

  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

  def modifyContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(ctxExtender)(fa)

  def extendContext[F[_]: Context, A](params: (String, HasLoggableEncoder)*)(fa: => F[A]): F[A] =
    modifyContext(_.addParameters(params: _*))(fa)
}
