package com.emarsys.logger.internal

import cats.Id
import com.emarsys.logger.{Logged, LoggingContext}
import munit.FunSuite

class LoggingContextMagnetSpec extends FunSuite {

  def getMagnet[F[_]](implicit m: LoggingContextMagnet[F]) = m

  test("LoggingContextMagnet should construct from LoggingContext") {
    assertEquals(compileErrors("""
        val lc: LoggingContext = LoggingContext("")
        getMagnet[Id](lc)
      """), "")
  }

  test("LoggingContextMagnet should construct from implicit LoggingContext") {
    assertEquals(compileErrors("""
        implicit val lc: LoggingContext = LoggingContext("")
        getMagnet[Id]
      """), "")
  }

  test("LoggingContextMagnet should construct from Context and Monad typeclasses") {
    assertEquals(compileErrors("""
        import cats.{Id, Monad}
        import com.emarsys.logger.Context

        implicit val m: Monad[Id] = null
        implicit val ctx: Context[Id] = null
        getMagnet
      """), "")
  }

  test("LoggingContextMagnet should return the context when constructed from a logging context") {
    val lc     = LoggingContext("")
    val magnet = getMagnet[Id](lc)

    var resultContext: LoggingContext = null
    magnet(ctx => resultContext = ctx)

    assertEquals(resultContext, lc)
  }

  test("LoggingContextMagnet should return the context when constructed from a monad and context typeclasses") {
    import cats.syntax.applicative._
    // the cats.catsInstancesForId import is only necessary for scala 3
    // FIXME: is this a scala 3 bug?
    import cats.catsInstancesForId

    val lc = LoggingContext("")

    val magnet = getMagnet[Logged[Id, *]]

    var resultContext: LoggingContext = null
    val a = magnet { ctx =>
      resultContext = ctx
      ().pure[Logged[Id, *]]
    }

    a.run(lc)

    assertEquals(resultContext, lc)
  }
}
