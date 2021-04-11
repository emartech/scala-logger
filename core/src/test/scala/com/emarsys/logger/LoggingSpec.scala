package com.emarsys.logger

import munit.FunSuite

class LoggingSpec extends FunSuite {

  test("Logging should compile with Id") {
    assertEquals(
      compileErrors("""
        import cats.Id
        import com.emarsys.logger.syntax._
        import com.emarsys.logger.unsafe.implicits._

        implicit val lc: LoggingContext = LoggingContext("")

        log[Id].warn("oh noe")
      """),
      ""
    )
  }

  test("Logging should compile with Future") {
    assertEquals(
      compileErrors("""
        import scala.concurrent.Future
        import scala.concurrent.ExecutionContext.Implicits.global
        import com.emarsys.logger.syntax._
        import com.emarsys.logger.unsafe.implicits._

        implicit val lc: LoggingContext = LoggingContext("")

        log[Future].warn("oh noe")
      """),
      ""
    )
  }

  // the cats.catsInstancesForId import is only necessary for scala 3
  // FIXME: is this a scala 3 bug?
  test("Logging should compile with Logged[Id]") {
    assertEquals(
      compileErrors("""
        import cats.Id
        import com.emarsys.logger.syntax._
        import com.emarsys.logger.unsafe.implicits._
        import cats.catsInstancesForId

        type LoggedId[A] = Logged[Id, A]

        log[LoggedId].warn("oh noe")
      """),
      ""
    )
  }

  test("Logging should compile with Logged[Future]") {
    assertEquals(
      compileErrors("""
        import scala.concurrent.Future
        import scala.concurrent.ExecutionContext.Implicits.global
        import com.emarsys.logger.syntax._
        import com.emarsys.logger.unsafe.implicits._

        type LoggedFuture[A] = Logged[Future, A]

        log[LoggedFuture].warn("oh noe")
      """),
      ""
    )
  }

}
