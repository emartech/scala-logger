package com.emarsys.logger

import munit.FunSuite

class ContextSpec extends FunSuite {

  test("Context should compile with ReaderT") {
    assertEquals(
      compileErrors("""
        import cats._
        import cats.data._

        type App[A] = ReaderT[Id, LoggingContext, A]

        implicitly[Context[App]]
      """),
      ""
    )
  }

  test("Context should compile with a stack of monad transformers containing ReaderT") {
    assertEquals(
      compileErrors("""
        import cats._
        import cats.data._

        type Reader[A] = ReaderT[Id, LoggingContext, A]
        type App[A] = EitherT[Reader, Throwable, A]

        implicitly[Context[App]]
      """),
      ""
    )
  }

}
