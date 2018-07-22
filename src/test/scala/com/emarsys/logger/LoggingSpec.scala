package com.emarsys.logger

import org.scalatest.{Matchers, WordSpec}

class LoggingSpec extends WordSpec with Matchers {

  "Logging" should {

    "compile with Id" in {
      """
        |import cats.Id
        |import com.emarsys.logger.implicits._
        |implicit val lc: LoggingContext = LoggingContext("")
        |
        |log[Id].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Future" in {
      """
        |import scala.concurrent.Future
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import cats.instances.future._
        |import com.emarsys.logger.implicits._
        |implicit val lc: LoggingContext = LoggingContext("")
        |
        |log[Future].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with IO" in {
      """
        |import cats.effect.IO
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import cats.instances.future._
        |import com.emarsys.logger.implicits._
        |implicit val lc: LoggingContext = LoggingContext("")
        |
        |log[IO].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Logged[Id]" in {
      """
        |import cats.Id
        |import com.emarsys.logger.implicits._
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
        |import cats.instances.future._
        |import com.emarsys.logger.implicits._
        |
        |type LoggedFuture[A] = Logged[Future, A]
        |
        |log[LoggedFuture].warn("oh noe")
      """.stripMargin should compile
    }

    "compile with Logged[IO]" in {
      """
        |import cats.effect.IO
        |import scala.concurrent.ExecutionContext.Implicits.global
        |import cats.instances.future._
        |import com.emarsys.logger.implicits._
        |
        |type LoggedIO[A] = Logged[IO, A]
        |
        |log[LoggedIO].warn("oh noe")
      """.stripMargin should compile
    }
  }

}
