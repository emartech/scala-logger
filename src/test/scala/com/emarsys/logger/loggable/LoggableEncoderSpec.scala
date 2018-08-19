package com.emarsys.logger.loggable
import org.scalacheck.Prop.forAll
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.prop.Checkers
import org.scalatest.{FreeSpec, Matchers}

class LoggableEncoderSpec extends FreeSpec with Checkers with Matchers with TypeCheckedTripleEquals {
  import LoggableEncoder.ops._

  "LoggableEncoder should be able to encode" - {
    "integers" in {
      check {
        forAll((n: Int) => n.toLoggable === LoggableIntegral(n.toLong))
      }
    }

    "longs" in {
      check {
        forAll((n: Long) => n.toLoggable === LoggableIntegral(n))
      }
    }

    "shorts" in {
      check {
        forAll((n: Short) => n.toLoggable === LoggableIntegral(n.toLong))
      }
    }

    "bytes" in {
      check {
        forAll((n: Byte) => n.toLoggable === LoggableIntegral(n.toLong))
      }
    }

    "floats" in {
      check {
        forAll((n: Float) => n.toLoggable === LoggableFloating(n.toDouble))
      }
    }

    "doubles" in {
      check {
        forAll((n: Double) => n.toLoggable === LoggableFloating(n))
      }
    }

    "booleans" in {
      check {
        forAll((b: Boolean) => b.toLoggable === LoggableBoolean(b))
      }
    }

    "strings" in {
      check {
        forAll((s: String) => s.toLoggable === LoggableString(s))
      }
    }

    "chars" in {
      check {
        forAll((c: Char) => c.toLoggable === LoggableString(c.toString))
      }
    }

    "symbols" in {
      val s = 'symbol
      s.toLoggable should ===(LoggableString(s.name))
    }

    "lists" in {
      check {
        forAll((l: List[Int]) => l.toLoggable === LoggableList(l.map(_.toLoggable)))
      }
    }

    "sets" in {
      check {
        forAll((l: Set[Int]) => l.toLoggable === LoggableList(l.toList.map(_.toLoggable)))
      }
    }

    "maps" in {
      check {
        forAll((l: Map[String, Int]) => l.toLoggable === LoggableObject(l.mapValues(_.toLoggable)))
      }
    }

    "options" in {
      check {
        forAll { o: Option[Int] =>
          val expected = o match {
            case Some(i) => i.toLoggable
            case None    => LoggableNil
          }

          o.toLoggable === expected
        }
      }
    }

    "eithers" in {
      check {
        forAll { o: Either[String, Int] =>
          val expected = o match {
            case Left(left)   => left.toLoggable
            case Right(right) => right.toLoggable
          }

          o.toLoggable === expected
        }
      }
    }

    "traversables" in {
      import cats.instances.vector._
      check {
        forAll((v: Vector[Int]) => v.toLoggable === LoggableList(v.toList.map(_.toLoggable)))
      }
    }
  }
}
