package com.emarsys

import cats.data.ReaderT
import cats.mtl.ApplicativeLocal
import cats.mtl.instances.LocalInstances
import com.emarsys.logger.LoggerSyntax
import com.emarsys.logger.loggable.LoggableEncoder

package object logger {

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]

  type Context[F[_]] = ApplicativeLocal[F, LoggingContext]

  object Context {
    def apply[F[_]](implicit ev: Context[F]): Context[F] = ev
  }

  object instances extends AllInstances

  object syntax extends AllSyntax

  object implicits extends AllInstances with AllSyntax
}

trait AllInstances extends LocalInstances

trait AllSyntax extends LoggerSyntax with LoggableEncoder.ToLoggableEncoderOps