package com.emarsys.logger
import cats.data.ReaderT
import cats.{Applicative, Id, MonadError}
import com.emarsys.logger.internal.LoggingContextMagnet

trait LoggerSyntax {
  import cats.syntax.applicativeError._
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def log[F[_]](implicit logging: Logging[F]): Logging[F]   = logging
  def unsafeLog(implicit logging: Logging[Id]): Logging[Id] = logging

  implicit class LogOps[F[_]: Logging: MonadError[?[_], Throwable], A](fa: F[A]) {
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

    def withContext(ctxExtender: LoggingContext => LoggingContext)(implicit ctx: Context[F]): F[A] =
      extendContext(ctxExtender)(fa)
  }

  implicit class LogConverter[F[_], A](fa: F[A]) {
    def toLogged: Logged[F, A] = withContext(_ => fa)
  }

  def withContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def getReaderContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

  def getContext[F[_]: Context]: F[LoggingContext] = Context[F].ask.ask

  def extendReaderContext[F[_], A](
      ctxExtender: LoggingContext => LoggingContext
  )(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

  def extendContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(ctxExtender)(fa)
}
