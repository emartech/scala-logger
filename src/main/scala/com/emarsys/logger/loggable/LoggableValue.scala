package com.emarsys.logger.loggable
import cats.Show
import cats.kernel.Eq

sealed abstract class LoggableValue {
  def widen: LoggableValue = this
}

object LoggableValue {
  implicit val eq: Eq[LoggableValue]     = Eq.fromUniversalEquals
  implicit val show: Show[LoggableValue] = Show.fromToString
}

final case class LoggableIntegral(value: Long)                   extends LoggableValue
final case class LoggableFloating(value: Double)                 extends LoggableValue
final case class LoggableString(value: String)                   extends LoggableValue
final case class LoggableBoolean(value: Boolean)                 extends LoggableValue
final case class LoggableList(list: List[LoggableValue])         extends LoggableValue
object LoggableList {
  def apply(elems: LoggableValue*): LoggableList = LoggableList(elems.toList)
  def empty: LoggableList = LoggableList(List.empty[LoggableValue])
}

final case class LoggableObject(obj: Map[String, LoggableValue]) extends LoggableValue
object LoggableObject {
  def apply(elems: (String, LoggableValue)*): LoggableObject = LoggableObject(elems.toMap)
  def empty: LoggableObject = LoggableObject.apply(Map.empty[String, LoggableValue])
}

case object LoggableNil                                          extends LoggableValue
