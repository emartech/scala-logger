package com.emarsys

import cats.data.ReaderT
import cats.mtl.ApplicativeLocal
import cats.mtl.instances.LocalInstances
import cats.{Applicative, Id, MonadError}
import com.emarsys.logger.internal.{LoggingContextMagnet, ToMapRec}
import com.emarsys.logger.loggable.LoggableEncoder
import com.emarsys.logger.{Context, Logged, Logging, LoggingContext}
import shapeless.{HList, LabelledGeneric, Lazy}

package object logger {

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]

  type Context[F[_]] = ApplicativeLocal[F, LoggingContext]

  object Context {
    def apply[F[_]](implicit ev: Context[F]): Context[F] = ev
  }

  object instances extends LoggerInstances

  object syntax extends LoggerSyntax with LoggableEncoder.ToLoggableEncoderOps

  object implicits extends LoggerInstances with LoggerSyntax
}

trait LoggerInstances extends LocalInstances

trait LoggerSyntax {
  import cats.syntax.applicativeError._
  import cats.syntax.apply._
  import cats.syntax.flatMap._

  def log[F[_]](implicit logging: Logging[F]): Logging[F]   = logging
  def unsafeLog(implicit logging: Logging[Id]): Logging[Id] = logging

  implicit class LogOps[F[_]: Logging: MonadError[?[_], Throwable], A](fa: F[A]) {
    def logFailure(implicit ctx: LoggingContext): F[A] = fa onError {
      case e: Throwable =>
        log.error(e)
    }

    def logFailure(msg: => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa onError {
      case e: Throwable =>
        log.error(e, msg)
    }

    def logFailure(createMsg: Throwable => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa onError {
      case e: Throwable =>
        log.error(e, createMsg(e))
    }

    def logSuccess(msg: => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa <* log.info(msg)

    def logSuccess(createMsg: A => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa flatTap { value =>
      log.info(createMsg(value))
    }
  }

  implicit class LogConverter[F[_], A](fa: F[A]) {
    def toLogged: Logged[F, A] = withLoggingContext(_ => fa)
  }

  def withLoggingContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

  def getLoggingContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

  def withExtendedLoggingContextRT[F[_], A](ctxExtender: LoggingContext => LoggingContext)(block: LoggingContext => F[A]): Logged[F, A] =
    ReaderT.local(ctxExtender)(ReaderT(block))

  def withExtendedLoggingContext[F[_]: Context, A](ctxExtender: LoggingContext => LoggingContext)(fa: => F[A]): F[A] =
    Context[F].local(ctxExtender)(fa)

  implicit class AnyToLoggingContextOps[A](a: A) {
    def toCtx[L <: HList](transactionId: String)(implicit gen: LabelledGeneric.Aux[A, L],
                                                 toMap: Lazy[ToMapRec[L]]): LoggingContext = {
      LoggingContext.fromData(a, transactionId)
    }

    def toCtx[L <: HList](transactionIdExtractor: A => String)(implicit gen: LabelledGeneric.Aux[A, L],
                                                               toMap: Lazy[ToMapRec[L]]): LoggingContext = {
      LoggingContext.fromData(a, transactionIdExtractor(a))
    }

    def toLoggable[L <: HList](implicit gen: LabelledGeneric.Aux[A, L], toMap: Lazy[ToMapRec[L]]): Map[String, Any] = {
      ToMapRec.toMap(a)
    }
  }

}
