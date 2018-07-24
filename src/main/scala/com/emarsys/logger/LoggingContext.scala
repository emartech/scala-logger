package com.emarsys.logger

import com.emarsys.logger.internal.ToMapRec
import shapeless.{HList, LabelledGeneric, Lazy}

case class LoggingContext(transactionID: String, logData: Map[String, Any] = Map.empty) {
  def +(param: (String, Any)): LoggingContext                    = addParameter(param)
  def addParameter[T <: Any](param: (String, T)): LoggingContext = addParameters(param)

  def ++(parameters: (String, Any)*): LoggingContext = addParameters(parameters: _*)
  def addParameters(parameters: (String, Any)*): LoggingContext =
    LoggingContext(transactionID, logData ++ parameters)
}

object LoggingContext {
  def fromData[Data, L <: HList](data: Data, transactionId: String)(implicit gen: LabelledGeneric.Aux[Data, L],
                                                                    toMap: Lazy[ToMapRec[L]]) = {
    val genericRepresentation = gen.to(data)
    val map                   = toMap.value(genericRepresentation)
    LoggingContext(transactionId, map)
  }
}
