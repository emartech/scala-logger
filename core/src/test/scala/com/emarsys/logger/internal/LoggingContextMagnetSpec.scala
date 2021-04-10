package com.emarsys.logger.internal

import cats.Id
import com.emarsys.logger.Logged
import com.emarsys.logger.LoggingContext
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class LoggingContextMagnetSpec extends AnyWordSpec with Matchers with TypeCheckedTripleEquals {

  def getMagnet[F[_]](implicit m: LoggingContextMagnet[F]) = m

  "LoggingContextMagnet" should {
    "construct from LoggingContext" in {
      """
        val lc: LoggingContext = LoggingContext("")
        getMagnet[Id](lc)
      """ should compile
    }

    "construct from implicit LoggingContext" in {
      """
        implicit val lc: LoggingContext = LoggingContext("")
        getMagnet[Id]
      """ should compile
    }

    "construct from Context and Monad typeclasses" in {
      """
        import cats.{Id, Monad}
        import com.emarsys.logger.Context

        implicit val m: Monad[Id] = null
        implicit val ctx: Context[Id] = null
        getMagnet
      """ should compile
    }

    "return the context when constructed from a logging context" in {
      val lc     = LoggingContext("")
      val magnet = getMagnet[Id](lc)

      var resultContext: LoggingContext = null
      magnet(ctx => resultContext = ctx)

      resultContext should ===(lc)
    }

    "return the context when constructed from a monad and context typeclasses" in {
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

      resultContext should ===(lc)
    }
  }
}
