package com.emarsys

import cats.data.ReaderT
import cats.mtl.Local
import com.emarsys.logger.LoggerSyntax
import com.emarsys.logger.loggable.LoggableEncoder

package object logger {
  type Context[F[_]] = Local[F, LoggingContext]

  object Context {
    def apply[F[_]](implicit ev: Context[F]): Context[F] = ev
  }

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]

  object syntax extends AllSyntax
}

trait AllSyntax extends LoggerSyntax with LoggableEncoder.ToLoggableEncoderOps
