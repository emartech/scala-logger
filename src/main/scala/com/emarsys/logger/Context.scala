package com.emarsys.logger
import cats.mtl.ApplicativeLocal

trait Context[F[_]] {
  def ask: F[LoggingContext]

  def local[A](extendContext: LoggingContext => LoggingContext)(fa: F[A]): F[A]
}

object Context extends DefaultContextInstances {

  @inline def apply[F[_]](implicit ev: Context[F]): Context[F] = ev

}

trait DefaultContextInstances {

  implicit def applicativeLocalContext[F[_]](implicit A: ApplicativeLocal[F, LoggingContext]): Context[F] =
    new Context[F] {
      override def ask: F[LoggingContext] = A.ask

      override def local[A](extendContext: LoggingContext => LoggingContext)(fa: F[A]): F[A] =
        A.local(extendContext)(fa)
    }

}
