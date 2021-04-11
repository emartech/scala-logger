package com.emarsys.logger.internal

import com.emarsys.logger.LoggingContext
import com.emarsys.logger.loggable._
import munit.FunSuite
import org.slf4j.Marker

import scala.jdk.CollectionConverters._

class LoggingContextUtilSpec extends FunSuite {

  test("#toMarker should add the transaction id to the marker") {
    val ctx = LoggingContext("trid")

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid}")
  }

  test("#toMarker should add primitive integrals to the marker") {
    val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1)))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {id=1}}")
  }

  test("#toMarker should add primitive integrals to the marker") {
    val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableString("uuid")))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {id=uuid}}")
  }

  test("#toMarker should add primitive floating point numbers to the marker") {
    val ctx = LoggingContext("trid", LoggableObject("metric" -> LoggableFloating(1.2)))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {metric=1.2}}")
  }

  test("#toMarker should add primitive booleans to the marker") {
    val ctx = LoggingContext("trid", LoggableObject("enabled" -> LoggableBoolean(true)))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {enabled=true}}")
  }

  test("#toMarker should add primitive nil to the marker") {
    val ctx = LoggingContext("trid", LoggableObject("result" -> LoggableNil))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {result=null}}")
  }

  test("#toMarker should add list of primitives to the marker") {
    val ctx =
      LoggingContext("trid", LoggableObject("values" -> LoggableList(LoggableIntegral(1), LoggableIntegral(2))))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {values=[1, 2]}}")
  }

  test("#toMarker should add objects to the marker") {
    val ctx =
      LoggingContext("trid", LoggableObject("person" -> LoggableObject("name" -> LoggableString("Joe"))))

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {person={name=Joe}}}")
  }

  test("#toMarker should add nested objects to the marker") {
    val ctx =
      LoggingContext(
        "trid",
        LoggableObject(
          "person" -> LoggableObject(
            "name"    -> LoggableString("Joe"),
            "address" -> LoggableObject("postcode" -> LoggableIntegral(1234))
          )
        )
      )

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {person={name=Joe, address={postcode=1234}}}}")
  }

  test("#toMarker should add list of objects to the marker") {
    val ctx =
      LoggingContext(
        "trid",
        LoggableObject(
          "values" -> LoggableList(
            LoggableObject("id" -> LoggableIntegral(1)),
            LoggableObject("id" -> LoggableIntegral(2))
          )
        )
      )

    val marker = LoggingContextUtil.toMarker(ctx)

    assertEquals(getMarkerJson(marker), "{transactionId=trid, {values=[{id=1}, {id=2}]}}")
  }

  private def getMarkerJson(marker: Marker): String = {
    val flatMarkers = flattenMarkers(marker)
    flatMarkers.map(_.toString).mkString("{", ", ", "}")
  }

  private def flattenMarkers(marker: Marker): List[Marker] =
    if (!marker.iterator().hasNext) {
      List(marker)
    } else {
      marker :: marker.iterator().asScala.drop(1).flatMap(flattenMarkers).toList
    }

}
