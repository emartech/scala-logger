package com.emarsys.logger

import com.emarsys.logger.loggable.{LoggableIntegral, LoggableObject, LoggableString}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingContextSpec extends AnyWordSpec with Matchers with TypeCheckedTripleEquals {

  "LoggingContext" should {
    "add log parameters via addParameter" in {
      val ctx = LoggingContext("trid")

      val extended = ctx.addParameter("id" -> 1)

      extended should ===(LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L))))
    }

    "add log parameters via + operator" in {
      val ctx = LoggingContext("trid")

      val extended = ctx <> "id" -> 1

      extended should ===(LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L))))
    }

    "allow chaining + operators" in {
      val ctx = LoggingContext("trid")

      val extended = ctx <> "id" -> 1 <> "name" -> "xy"

      extended should ===(
        LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L), "name" -> LoggableString("xy")))
      )
    }

    "allow chaining addParameter calls" in {
      val ctx = LoggingContext("trid")

      val extended = ctx.addParameter("id" -> 1).addParameter("name" -> "xy")

      extended should ===(
        LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L), "name" -> LoggableString("xy")))
      )
    }
  }

}
