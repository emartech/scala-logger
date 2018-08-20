package com.emarsys.logger

import com.emarsys.logger.internal.ToMapRec
import com.emarsys.logger.loggable.{LoggableEncoder, LoggableObject}
import shapeless.{HList, LabelledGeneric, Lazy}

case class LoggingContext(transactionID: String, logData: LoggableObject = LoggableObject(Map.empty)) {
  import cats.implicits._
  import LoggableEncoder.ops._

  def +[T: LoggableEncoder](param: (String, T)): LoggingContext = addParameter(param)
  def addParameter[T: LoggableEncoder](param: (String, T)): LoggingContext = {
    val encodedParam = param.map(_.toLoggable)
    copy(logData = LoggableObject(logData.obj + encodedParam))
  }
}

object LoggingContext {
  def fromData[Data, L <: HList](data: Data, transactionId: String)(implicit gen: LabelledGeneric.Aux[Data, L],
                                                                    toMap: Lazy[ToMapRec[L]]): LoggingContext = {
//    val map = ToMapRec.toMap(data)
//    LoggingContext(transactionId, map)
    ???
  }
}
