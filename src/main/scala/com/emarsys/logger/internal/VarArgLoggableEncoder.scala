package com.emarsys.logger.internal

import com.emarsys.logger.loggable.LoggableEncoder

import scala.annotation.implicitAmbiguous
import scala.language.implicitConversions

trait VarArgLoggableEncoder extends VarArgLoggableEncoder1 {
  implicit def hasLoggableEncoder[A](a: A)(implicit encoder: LoggableEncoder[A]): HasLoggableEncoder =
    encoder.toLoggable(a).asInstanceOf[HasLoggableEncoder]
}

trait VarArgLoggableEncoder1 {
  type HasLoggableEncoder // = LoggableValue

  @implicitAmbiguous("""
  Cannot add a value of type ${A} to a logging context, as no implicit
  LoggableEncoder[${A}] instance is in scope.

  If you wish to create a LoggableEncoder instance for your class, in case ${A}
  is a case class or a sealed trait hierarchy, you may use the
  LoggableEncoder.deriveLoggableEncoder method to automatically generate it.
  """)
  implicit def ambiguousLoggableEncoder1[A](a: A): HasLoggableEncoder = ???
  implicit def ambiguousLoggableEncoder2[A](a: A): HasLoggableEncoder = ???
}
