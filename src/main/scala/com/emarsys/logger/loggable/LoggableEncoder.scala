package com.emarsys.logger.loggable

import cats.syntax.all._
import cats.{Contravariant, Traverse}
import com.emarsys.logger.loggable.LoggableEncoder.ops.toAllLoggableEncoderOps
import simulacrum.typeclass

@typeclass trait LoggableEncoder[A] {
  def toLoggable(a: A): LoggableValue
}

object LoggableEncoder extends LoggableEncoderStdlib1 with LoggableEncoderStdlib2 {

  implicit val contravariantLoggableEncoder: Contravariant[LoggableEncoder] = new Contravariant[LoggableEncoder] {
    override def contramap[A, B](fa: LoggableEncoder[A])(f: B => A): LoggableEncoder[B] = b => fa.toLoggable(f(b))
  }

  implicit val loggableValue: LoggableEncoder[LoggableValue] = identity[LoggableValue]
  implicit val long: LoggableEncoder[Long]                   = LoggableIntegral(_)
  implicit val double: LoggableEncoder[Double]               = LoggableFloating(_)
  implicit val boolean: LoggableEncoder[Boolean]             = LoggableBoolean(_)
  implicit val string: LoggableEncoder[String]               = LoggableString(_)

  implicit val int: LoggableEncoder[Int]       = long.contramap(_.toLong)
  implicit val short: LoggableEncoder[Short]   = long.contramap(_.toLong)
  implicit val byte: LoggableEncoder[Byte]     = long.contramap(_.toLong)
  implicit val unit: LoggableEncoder[Unit]     = long.contramap(_ => 1)
  implicit val float: LoggableEncoder[Float]   = double.contramap(_.toDouble)
  implicit val char: LoggableEncoder[Char]     = string.contramap(_.toString)
  implicit val symbol: LoggableEncoder[Symbol] = string.contramap(_.name)
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
    m => LoggableObject(m.mapValues(_.toLoggable))

}

private[loggable] trait LoggableEncoderStdlib2 {
  self: LoggableEncoderStdlib1 =>

  implicit def traversable[T[_]: Traverse, A: LoggableEncoder]: LoggableEncoder[T[A]] = list[A].contramap(_.toList)
}
