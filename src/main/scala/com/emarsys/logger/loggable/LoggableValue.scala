package com.emarsys.logger.loggable
import cats.kernel.Eq

sealed abstract class LoggableValue {
  def widen: LoggableValue = this
}

object LoggableValue {
  implicit val eq: Eq[LoggableValue] = Eq.fromUniversalEquals
}

final case class LoggableIntegral(value: Long) extends LoggableValue
final case class LoggableFloating(value: Double) extends LoggableValue
final case class LoggableString(value: String) extends LoggableValue
final case class LoggableBoolean(value: Boolean) extends LoggableValue
final case class LoggableList(list: List[LoggableValue]) extends LoggableValue
final case class LoggableObject(obj: Map[String, LoggableValue]) extends LoggableValue
case object LoggableNil extends LoggableValue