package com.emarsys.logger.loggable

import java.time._

import org.scalacheck.Prop.forAll
import org.scalactic.TypeCheckedTripleEquals
import org.scalatestplus.scalacheck.Checkers
import org.scalatest.{FreeSpec, Matchers}
import com.emarsys.logger.testutil.Arbitraries._

class LoggableEncoderSpec extends FreeSpec with Checkers with Matchers with TypeCheckedTripleEquals {
  import LoggableEncoder.ops._

  "LoggableEncoder should" - {
    "be able to encode" - {
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
          forAll((l: Map[String, Int]) => l.toLoggable === LoggableObject(l map { case (k, v) => (k, v.toLoggable) }))
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

      "instant" in {
        check {
          forAll(instantGen)((dt: Instant) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "localDate" in {
        check {
          forAll(localDateGen)((dt: LocalDate) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "localTime" in {
        check {
          forAll(localTimeGen)((dt: LocalTime) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "localDateTime" in {
        check {
          forAll(localDateTimeGen)((dt: LocalDateTime) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "zonedDateTime" in {
        check {
          forAll(zonedDateTimeGen)((dt: ZonedDateTime) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "offsetDateTime" in {
        check {
          forAll(offsetDateTimeGen)((dt: OffsetDateTime) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "offsetTime" in {
        check {
          forAll(offsetTimeGen)((dt: OffsetTime) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "jdkDuration" in {
        check {
          forAll(durationGen)((dt: java.time.Duration) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "scalaDuration" in {
        check {
          forAll((dt: scala.concurrent.duration.Duration) => dt.toLoggable === LoggableString(dt.toString))
        }
      }

      "scalaFiniteDuration" in {
        check {
          forAll((dt: scala.concurrent.duration.FiniteDuration) => dt.toLoggable === LoggableString(dt.toString))
        }
      }
    }

    "generate LoggableEncoder instance for" - {
      "simple case class" in {
        case class Simple(i: Int)

        val encoder: LoggableEncoder[Simple] = LoggableEncoder.deriveLoggableEncoder

        val simple = Simple(42)

        encoder.toLoggable(simple) should ===(LoggableObject("i" -> LoggableIntegral(42)))
      }

      "nested case class" in {
        case class Simple(i: Int)
        case class Nested(s: Simple)

        implicit val simpleEncoder: LoggableEncoder[Simple] = LoggableEncoder.deriveLoggableEncoder
        val nestedEncoder: LoggableEncoder[Nested]          = LoggableEncoder.deriveLoggableEncoder

        val nested = Nested(Simple(42))

        nestedEncoder.toLoggable(nested) should ===(
          LoggableObject("s" -> LoggableObject("i" -> LoggableIntegral(42)))
        )
      }

      "ADT" in {
        sealed trait T
        case class A(x: Int)    extends T
        case class B(y: String) extends T

        implicit val encoder: LoggableEncoder[T] = LoggableEncoder.deriveLoggableEncoder

        encoder.toLoggable(A(42)) should ===(LoggableObject("x"   -> LoggableIntegral(42)))
        encoder.toLoggable(B("42")) should ===(LoggableObject("y" -> LoggableString("42")))
      }
    }

    "handle null for" - {
      "simple case class" in {
        case class Simple(i: Int)

        implicit val encoder: LoggableEncoder[Simple] = LoggableEncoder.deriveLoggableEncoder

        encoder.toLoggable(null) should ===(LoggableNil)
      }

      "nested case class" in {
        case class Simple(i: Int)
        case class Nested(s: Simple)

        implicit val simpleEncoder: LoggableEncoder[Simple] = LoggableEncoder.deriveLoggableEncoder
        val nestedEncoder: LoggableEncoder[Nested]          = LoggableEncoder.deriveLoggableEncoder

        val nested = Nested(null)

        nestedEncoder.toLoggable(nested) should ===(LoggableObject("s" -> LoggableNil))
      }

      "ADT" in {
        sealed trait T
        case class A(x: Int)    extends T
        case class B(y: String) extends T

        implicit val encoder: LoggableEncoder[T] = LoggableEncoder.deriveLoggableEncoder

        encoder.toLoggable(null) should ===(LoggableNil)
      }
    }
  }
}
