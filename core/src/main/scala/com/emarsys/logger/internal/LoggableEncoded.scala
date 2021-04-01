package com.emarsys.logger.internal

import com.emarsys.logger.loggable.{LoggableEncoder, LoggableValue}

import scala.annotation.implicitAmbiguous
import scala.language.implicitConversions

final case class LoggableEncoded private[internal] (loggableValue: LoggableValue) extends AnyVal

object LoggableEncoded extends AmbiguousLoggableEncodedConversion {

  implicit def materialize[A](a: A)(implicit enc: LoggableEncoder[A]): LoggableEncoded =
    LoggableEncoded(enc.toLoggable(a))
}

private[internal] trait AmbiguousLoggableEncodedConversion {

  @implicitAmbiguous("""
  Cannot add a value of type ${A} to a logging context, as no implicit
  LoggableEncoder[${A}] instance is in scope.

  If you wish to create a LoggableEncoder instance for your class, in case ${A}
  is a case class or a sealed trait hierarchy, you may use the
  LoggableEncoder.deriveLoggableEncoder method to automatically generate it.
  """)
  implicit def ambiguousLoggableEncodedMaterializer1[A](a: A): LoggableEncoded = ???
  implicit def ambiguousLoggableEncodedMaterializer2[A](a: A): LoggableEncoded = ???
}
