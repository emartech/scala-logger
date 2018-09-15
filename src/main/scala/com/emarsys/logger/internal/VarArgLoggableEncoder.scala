package com.emarsys.logger.internal

import com.emarsys.logger.loggable.LoggableEncoder

import scala.annotation.implicitAmbiguous
import scala.language.implicitConversions

trait VarArgLoggableEncoder extends VarArgLoggableEncoder1 {
  implicit def hasLoggedValue[A](a: A)(implicit encoder: LoggableEncoder[A]): HasLoggableEncoder =
    encoder.toLoggable(a).asInstanceOf[HasLoggableEncoder]
}

trait VarArgLoggableEncoder1 {
  type HasLoggableEncoder // = LoggableValue

  @implicitAmbiguous(
    "Cannot add value of type ${A} to a logging context, as no implicit LoggableEncoder instance is in scope."
  )
  implicit def ambiguousLoggedValue1[A](a: A): HasLoggableEncoder = ???
  implicit def ambiguousLoggedValue2[A](a: A): HasLoggableEncoder = ???
}
