package com.emarsys

import cats.data.ReaderT
import cats.effect.IO
import cats.mtl.Local
import com.emarsys.logger.LoggerSyntax
import com.emarsys.logger.loggable.LoggableEncoder

package object logger {
  type Context[F[_]] = Local[F, LoggingContext]

  object Context {
    def apply[F[_]](implicit C: Context[F]) = C
  }

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]
  type LoggedIO[A]     = Logged[IO, A]

  object syntax extends AllSyntax
}

trait AllSyntax extends LoggerSyntax with LoggableEncoder.ToLoggableEncoderOps
