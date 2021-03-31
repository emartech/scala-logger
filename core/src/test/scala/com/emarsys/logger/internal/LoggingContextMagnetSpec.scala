package com.emarsys.logger.internal

import cats.{Id, Monad}
import com.emarsys.logger.{Context, Logged, LoggingContext}
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class LoggingContextMagnetSpec extends AnyWordSpec with Matchers with TypeCheckedTripleEquals {

  def getMagnet[F[_]](implicit m: LoggingContextMagnet[F]) = m

  "LoggingContextMagnet" should {
    "construct from LoggingContext" in {
      """
        |  val lc: LoggingContext = LoggingContext("")
        |  getMagnet[Id](lc)
      """.stripMargin should compile
    }

    "construct from implicit LoggingContext" in {
      """
        |  implicit val lc: LoggingContext = LoggingContext("")
        |  getMagnet[Id]
      """.stripMargin should compile
    }

    "construct from Context and Monad typeclasses" in {
      """
        |  import cats.{Id, Monad}
        |
        |  implicit val m: Monad[Id] = null
        |  implicit val ctx: Context[Id] = null
        |  getMagnet
      """.stripMargin should compile
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

      val lc = LoggingContext("")

      implicit val m = Monad[Logged[Id, *]]
      implicit val c = Context[Logged[Id, *]]
      val magnet     = getMagnet

      var resultContext: LoggingContext = null
      val a = magnet { ctx =>
        resultContext = ctx
        ().pure
      }

      a.run(lc)

      resultContext should ===(lc)
    }
  }
}
