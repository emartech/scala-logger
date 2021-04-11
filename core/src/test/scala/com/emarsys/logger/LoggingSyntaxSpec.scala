package com.emarsys.logger

import munit.FunSuite

class LoggingSyntaxSpec extends FunSuite {

  test("LoggingSyntax should compile extendContext") {
    assertEquals(
      compileErrors("""
        import cats._
        import com.emarsys.logger.syntax._

        def f[F[_]: Applicative: Logging: Context]() = {
          extendContext("id" -> 1) {
            Applicative[F].unit
          }
        }
      """),
      ""
    )
  }

  test("LoggingSyntax should compile withExtendedContext") {
    assertEquals(
      compileErrors("""
        import cats._
        import com.emarsys.logger.syntax._

        def f[F[_]: Applicative: Logging: Context]() = {
          Applicative[F].unit.withExtendedContext("id" -> 1)
        }
      """),
      ""
    )
  }

  test("LoggingSyntax should compile addParameters") {
    assertEquals(
      compileErrors("""
        import cats._
        import com.emarsys.logger.syntax._

        val lc = LoggingContext("trid")

        lc.addParameters("id" -> 1)
      """),
      ""
    )

  }
}
