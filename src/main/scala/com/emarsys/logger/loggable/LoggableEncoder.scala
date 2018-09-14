package com.emarsys.logger.loggable

import java.time._

import cats.syntax.all._
import cats.{Contravariant, Show, Traverse}
import com.emarsys.logger.loggable.LoggableEncoder.ops.toAllLoggableEncoderOps
import simulacrum.typeclass

import scala.language.implicitConversions

@typeclass trait LoggableEncoder[A] {
  def toLoggable(a: A): LoggableValue
}

object LoggableEncoder
    extends LoggableEncoderStdlib1
    with LoggableEncoderJdk8DateTime
    with LoggableEncoderScalaDuration
    with LoggableEncoderStdlib2
    with GenericLoggableEncoder {

  implicit val contravariantLoggableEncoder: Contravariant[LoggableEncoder] = new Contravariant[LoggableEncoder] {
    override def contramap[A, B](fa: LoggableEncoder[A])(f: B => A): LoggableEncoder[B] = b => fa.toLoggable(f(b))
  }

  implicit lazy val loggableValue: LoggableEncoder[LoggableValue] = identity[LoggableValue]
  implicit lazy val long: LoggableEncoder[Long]                   = LoggableIntegral(_)
  implicit lazy val double: LoggableEncoder[Double]               = LoggableFloating(_)
  implicit lazy val boolean: LoggableEncoder[Boolean]             = LoggableBoolean(_)
  implicit lazy val string: LoggableEncoder[String]               = LoggableString(_)

  def fromToString[A]: LoggableEncoder[A]   = string.contramap[A](_.toString)
  def fromShow[A: Show]: LoggableEncoder[A] = string.contramap[A](_.show)

  implicit lazy val int: LoggableEncoder[Int]       = long.contramap(_.toLong)
  implicit lazy val short: LoggableEncoder[Short]   = long.contramap(_.toLong)
  implicit lazy val byte: LoggableEncoder[Byte]     = long.contramap(_.toLong)
  implicit lazy val unit: LoggableEncoder[Unit]     = long.contramap(_ => 1)
  implicit lazy val float: LoggableEncoder[Float]   = double.contramap(_.toDouble)
  implicit lazy val char: LoggableEncoder[Char]     = fromToString
  implicit lazy val symbol: LoggableEncoder[Symbol] = string.contramap(_.name)
}

private[loggable] trait LoggableEncoderStdlib1 {
  self: LoggableEncoder.type =>

  implicit def option[A: LoggableEncoder]: LoggableEncoder[Option[A]] = {
    case Some(value) => value.toLoggable
    case None        => LoggableNil
  }

  implicit def either[A: LoggableEncoder, B: LoggableEncoder]: LoggableEncoder[Either[A, B]] = {
    case Left(value)  => value.toLoggable
    case Right(value) => value.toLoggable
  }

  implicit def list[A: LoggableEncoder]: LoggableEncoder[List[A]] = l => LoggableList(l.map(_.toLoggable))

  implicit def set[A: LoggableEncoder]: LoggableEncoder[Set[A]] = list[A].contramap(_.toList)

  implicit def dict[A: LoggableEncoder]: LoggableEncoder[Map[String, A]] =
    m => LoggableObject(m.map { case (k, v) => (k, v.toLoggable) })
}

private[loggable] trait LoggableEncoderJdk8DateTime {
  self: LoggableEncoder.type =>

  implicit lazy val instant: LoggableEncoder[Instant]                = fromToString
  implicit lazy val localDate: LoggableEncoder[LocalDate]            = fromToString
  implicit lazy val localTime: LoggableEncoder[LocalTime]            = fromToString
  implicit lazy val localDateTime: LoggableEncoder[LocalDateTime]    = fromToString
  implicit lazy val zonedDateTime: LoggableEncoder[ZonedDateTime]    = fromToString
  implicit lazy val offsetTime: LoggableEncoder[OffsetTime]          = fromToString
  implicit lazy val offsetDateTime: LoggableEncoder[OffsetDateTime]  = fromToString
  implicit lazy val jdkduration: LoggableEncoder[java.time.Duration] = fromToString
}

private[loggable] trait LoggableEncoderScalaDuration {
  self: LoggableEncoder.type =>

  implicit lazy val scalaFiniteDuration: LoggableEncoder[scala.concurrent.duration.FiniteDuration] = fromToString
  implicit lazy val scalaDuration: LoggableEncoder[scala.concurrent.duration.Duration]             = fromToString
}

private[loggable] trait LoggableEncoderStdlib2 {
  self: LoggableEncoderStdlib1 =>

  implicit def traversable[T[_]: Traverse, A: LoggableEncoder]: LoggableEncoder[T[A]] = list[A].contramap(_.toList)
}
