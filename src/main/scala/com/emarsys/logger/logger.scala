package com.emarsys

import cats.data.ReaderT
import com.emarsys.logger.LoggerSyntax
import com.emarsys.logger.loggable.LoggableEncoder

package object logger {

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]

  object instances extends AllInstances

  object syntax extends AllSyntax

  object implicits extends AllInstances with AllSyntax
}

trait AllInstances extends cats.mtl.instances.AllInstances

trait AllSyntax extends LoggerSyntax with LoggableEncoder.ToLoggableEncoderOps
