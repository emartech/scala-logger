package com.emarsys.logger

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class ContextSpec extends AnyWordSpec with Matchers {

  "Context" should {

    "compile with ReaderT" in {
      """
        import cats._
        import cats.data._

        type App[A] = ReaderT[Id, LoggingContext, A]

        implicitly[Context[App]]
      """ should compile
    }

    "compile with a stack of monad transformers containing ReaderT" in {
      """
        import cats._
        import cats.data._

        type Reader[A] = ReaderT[Id, LoggingContext, A]
        type App[A] = EitherT[Reader, Throwable, A]

        implicitly[Context[App]]
      """ should compile
    }
  }

}
