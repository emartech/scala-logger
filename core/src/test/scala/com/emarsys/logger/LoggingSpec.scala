package com.emarsys.logger

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingSpec extends AnyWordSpec with Matchers {

  "Logging" should {

    "compile with Id" in {
      """
        |import cats.Id
        |import com.emarsys.logger.syntax._
        |import com.emarsys.logger.unsafe.implicits._
        |
        |implicit val lc: LoggingContext = LoggingContext("")
        |
        |log[Id].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Future" in {
      """
        |import scala.concurrent.Future
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import com.emarsys.logger.syntax._
        |import com.emarsys.logger.unsafe.implicits._
        |
        |implicit val lc: LoggingContext = LoggingContext("")
        |
        |log[Future].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Logged[Id]" in {
      """
        |import cats.Id
        |import com.emarsys.logger.syntax._
        |import com.emarsys.logger.unsafe.implicits._
        |
        |type LoggedId[A] = Logged[Id, A]
        |
        |log[LoggedId].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Logged[Future]" in {
      """
        |import scala.concurrent.Future
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import com.emarsys.logger.syntax._
        |import com.emarsys.logger.unsafe.implicits._
        |
        |type LoggedFuture[A] = Logged[Future, A]
        |
        |log[LoggedFuture].warn("oh noe")
      """.stripMargin should compile
    }
  }

}
