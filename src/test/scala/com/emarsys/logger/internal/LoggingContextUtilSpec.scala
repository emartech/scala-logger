package com.emarsys.logger.internal

import com.emarsys.logger.LoggingContext
import com.emarsys.logger.loggable._
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}
import org.slf4j.Marker

import scala.collection.JavaConverters._

class LoggingContextUtilSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {

  "#toMarker" should {
    "add the transaction id to the marker" in {
      val ctx = LoggingContext("trid")

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid}")
    }

    "add primitive integrals to the marker" in {
      val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1)))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {id=1}}")
    }

    "add primitive strings to the marker" in {
      val ctx = LoggingContext("trid", LoggableObject("id" -> LoggableString("uuid")))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {id=uuid}}")
    }

    "add primitive floating point numbers to the marker" in {
      val ctx = LoggingContext("trid", LoggableObject("metric" -> LoggableFloating(1.2)))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {metric=1.2}}")
    }

    "add primitive booleans to the marker" in {
      val ctx = LoggingContext("trid", LoggableObject("enabled" -> LoggableBoolean(true)))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {enabled=true}}")
    }

    "add primitive nil to the marker" in {
      val ctx = LoggingContext("trid", LoggableObject("result" -> LoggableNil))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {result=null}}")
    }

    "add list of primitives to the marker" in {
      val ctx =
        LoggingContext("trid", LoggableObject("values" -> LoggableList(LoggableIntegral(1), LoggableIntegral(2))))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {values=[1, 2]}}")
    }

    "add objects to the marker" in {
      val ctx =
        LoggingContext("trid", LoggableObject("person" -> LoggableObject("name" -> LoggableString("Joe"))))

      val marker = LoggingContextUtil.toMarker(ctx)

      getMarkerJson(marker) should ===("{transactionId=trid, {person={name=Joe}}}")
    }

    "add nested objects to the marker" in {
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

      getMarkerJson(marker) should ===("{transactionId=trid, {person={name=Joe, address={postcode=1234}}}}")
    }

    "add list of objects to the marker" in {
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

      getMarkerJson(marker) should ===("{transactionId=trid, {values=[{id=1}, {id=2}]}}")
    }
  }

  private def getMarkerJson(marker: Marker): String = {
    val flatMarkers = flattenMarkers(marker)
    flatMarkers.map(_.toString).mkString("{", ", ", "}")
  }

  private def flattenMarkers(marker: Marker): List[Marker] =
    if (!marker.iterator().hasNext) {
      List(marker)
    } else {
      marker :: marker.iterator().asScala.flatMap(flattenMarkers).toList
    }

}
