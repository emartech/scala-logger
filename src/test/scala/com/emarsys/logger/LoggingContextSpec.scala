package com.emarsys.logger

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}


class LoggingContextSpec extends WordSpec with Matchers with TypeCheckedTripleEquals {

  "LoggingContext" should {
    "add log parameters via addParameter" in {
      val ctx = LoggingContext("trid")

      val extended = ctx.addParameter("id" -> 1)

      extended should === (LoggingContext("trid", Map("id" -> 1)))
    }

    "add log parameters via + operator" in {
      val ctx = LoggingContext("trid")

      val extended = ctx + ("id" -> 1)

      extended should === (LoggingContext("trid", Map("id" -> 1)))
    }

    "add multiple log parameters via addParameters" in {
      val ctx = LoggingContext("trid")

      val extended = ctx.addParameters("id" -> 1, "name" -> "Joe")

      extended should === (LoggingContext("trid", Map("id" -> 1, "name" -> "Joe")))
    }

    "add multiple log parameters via ++" in {
      val ctx = LoggingContext("trid")

      val extended = ctx ++ ("id" -> 1, "name" -> "Joe")

      extended should === (LoggingContext("trid", Map("id" -> 1, "name" -> "Joe")))
    }
  }

}
