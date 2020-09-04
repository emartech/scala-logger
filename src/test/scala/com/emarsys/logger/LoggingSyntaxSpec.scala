package com.emarsys.logger

import cats.Applicative
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingSyntaxSpec extends AnyWordSpec with Matchers {
  "LoggingSyntax" should {
    "compile extendContext" in {
      """
        | import com.emarsys.logger.syntax._
        |
        | def f[F[_]: Applicative: Logging: Context]() = {
        |   extendContext("id" -> 1) {
        |     Applicative[F].unit
        |   }
        | }
      """.stripMargin should compile
    }

    "compile withExtendedContext" in {
      """
        | import com.emarsys.logger.syntax._
        |
        | def f[F[_]: Applicative: Logging: Context]() = {
        |   Applicative[F].unit.withExtendedContext("id" -> 1)
        | }
      """.stripMargin should compile
    }

    "compile addParameters" in {
      """
        | import com.emarsys.logger.syntax._
        |
        | val lc = LoggingContext("trid")
        |
        | lc.addParameters("id" -> 1)
      """.stripMargin should compile

    }
  }
}
