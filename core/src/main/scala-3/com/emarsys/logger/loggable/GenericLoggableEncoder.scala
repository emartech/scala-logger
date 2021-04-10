package com.emarsys.logger.loggable

import scala.deriving.Mirror
import scala.compiletime._

private [loggable] trait GenericLoggableEncoder:
  inline final def derived[A](using m: Mirror.Of[A]): LoggableEncoder[A] =
    new LoggableEncoder[A]:
      private[this] lazy val encoderInstances = summonLoggableEncoders[m.MirroredElemTypes].toArray
      private[this] lazy val labels           = summonLabels[m.MirroredElemLabels].toArray

      def toLoggable(a: A) =
        if a == null then
          LoggableNil
        else
          inline m match
            case s: Mirror.SumOf[A]     => derivedSum(a, s, encoderInstances)
            case _: Mirror.ProductOf[A] => derivedProduct(a, labels, encoderInstances)
  end derived

  inline final def summonLoggableEncoders[A <: Tuple]: List[LoggableEncoder[_]] =
   inline erasedValue[A] match
      case _: EmptyTuple => Nil
      case _: (a *: as)  => summonLoggableEncoder[a] :: summonLoggableEncoders[as]

  inline final def summonLoggableEncoder[A]: LoggableEncoder[A] = 
    summonFrom {
      case aEncoder: LoggableEncoder[A] => aEncoder
      case _: Mirror.Of[A]              => derived[A]
    }

  inline final def summonLabels[T <: Tuple]: List[String] =
    inline erasedValue[T] match
      case _: EmptyTuple => Nil
      case _: (t *: ts)  => constValue[t].asInstanceOf[String] :: summonLabels[ts]

  private def derivedSum[A](a: A, s: Mirror.SumOf[A], encoders: Array[LoggableEncoder[_]]): LoggableValue =
    val index = s.ordinal(a)
    val encoder  = encoders(index)
    encoder.asInstanceOf[LoggableEncoder[Any]].toLoggable(a)

  private def derivedProduct[A](a: A, labels: Array[String], encoders: Array[LoggableEncoder[_]]): LoggableValue =
    val iter = a.asInstanceOf[Product].productIterator.zip(labels)
    val map  = iter.zip(encoders.iterator).foldLeft(Map.empty[String, LoggableValue]) { 
      case (acc, ((field, label), encoder)) =>
        val encoded = 
          if field == null then
            LoggableNil
          else
            encoder.asInstanceOf[LoggableEncoder[Any]].toLoggable(field)
        acc + (label -> encoded)
    }
    LoggableObject(map)
  end derivedProduct