package com.emarsys.logger

import com.emarsys.logger.loggable.{LoggableIntegral, LoggableObject, LoggableString}
import munit.FunSuite

class LoggingContextSpec extends FunSuite {

  test("LoggingContext should add log parameters via addParameter") {
    val ctx = LoggingContext("trid")

    val extended = ctx.addParameter("id" -> 1)

    assertEquals(extended, LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L))))
  }

  test("LoggingContext should add log parameters via + operator") {
    val ctx = LoggingContext("trid")

    val extended = ctx <> "id" -> 1

    assertEquals(extended, LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L))))
  }

  test("LoggingContext should allow chaining + operators") {
    val ctx = LoggingContext("trid")

    val extended = ctx <> "id" -> 1 <> "name" -> "xy"

    assertEquals(
      extended,
      LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L), "name" -> LoggableString("xy")))
    )
  }

  test("LoggingContext should allow chaining addParameter calls") {
    val ctx = LoggingContext("trid")

    val extended = ctx.addParameter("id" -> 1).addParameter("name" -> "xy")

    assertEquals(
      extended,
      LoggingContext("trid", LoggableObject("id" -> LoggableIntegral(1L), "name" -> LoggableString("xy")))
    )
  }

}
