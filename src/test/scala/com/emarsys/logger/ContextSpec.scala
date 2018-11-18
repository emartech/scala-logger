package com.emarsys.logger

import org.scalatest.{Matchers, WordSpec}

class ContextSpec extends WordSpec with Matchers {

  "Context" should {

    "compile with ReaderT" in {
      """
        |import cats._
        |import cats.data._
        |import com.emarsys.logger.implicits._
        |
        |type App[A] = ReaderT[Id, LoggingContext, A]
        |
        |implicitly[Context[App]]
      """.stripMargin should compile
    }

    "compile with a stack of monad transformers containing ReaderT" in {
      """
        |import cats._
        |import cats.data._
        |import com.emarsys.logger.implicits._
        |
        |type Reader[A] = ReaderT[Id, LoggingContext, A]
        |type App[A] = EitherT[Reader, Throwable, A]
        |
        |implicitly[Context[App]]
      """.stripMargin should compile
    }
  }

}
