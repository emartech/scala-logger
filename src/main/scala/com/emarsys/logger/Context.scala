package com.emarsys.logger

import cats.Applicative
import cats.data.ReaderT

trait Context[F[_]] {
  def ask: F[LoggingContext]
  def local[A](f: LoggingContext => LoggingContext)(fa: => F[A]): F[A]
}

object Context extends ReaderTContextInstance {
  def apply[F[_]](implicit ev: Context[F]): Context[F] = ev
}
trait ReaderTContextInstance {
  implicit def rtContext[F[_]: Applicative]: Context[ReaderT[F, LoggingContext, ?]] =
    new Context[ReaderT[F, LoggingContext, ?]] {
      override def ask: ReaderT[F, LoggingContext, LoggingContext] = ReaderT.ask[F, LoggingContext]
      override def local[A](f: LoggingContext => LoggingContext)(
          fa: => ReaderT[F, LoggingContext, A]): ReaderT[F, LoggingContext, A] = ReaderT.local(f)(fa)
    }
}
