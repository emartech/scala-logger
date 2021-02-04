package com.emarsys.logger.loggable

import magnolia.{CaseClass, Magnolia, SealedTrait}

import scala.language.experimental.macros

trait GenericLoggableEncoder {
  type Typeclass[A] = LoggableEncoder[A]

  def combine[A](ctx: CaseClass[LoggableEncoder, A]): LoggableEncoder[A] = {
    case null => LoggableNil
    case a =>
      val empty = Map.empty[String, LoggableValue]
      val fields = ctx.parameters.foldRight(empty) { (p, acc) =>
        val value = p.typeclass.toLoggable(p.dereference(a))
        acc + (p.label -> value)
      }

      LoggableObject(fields)
  }

  def dispatch[A](ctx: SealedTrait[LoggableEncoder, A]): LoggableEncoder[A] = {
    case null => LoggableNil
    case a =>
      ctx.dispatch(a)(sub => sub.typeclass.toLoggable(sub.cast(a)))
  }

  def deriveLoggableEncoder[A]: LoggableEncoder[A] = macro Magnolia.gen[A]
}
