package com.emarsys.logger.loggable

import munit.{Location, ScalaCheckSuite}
import org.scalacheck.Prop.forAll
import org.scalacheck.util.Pretty
import org.scalacheck.{Arbitrary, Shrink}

import java.time._
import scala.annotation.nowarn
import scala.reflect.ClassTag

class LoggableEncoderSpec extends ScalaCheckSuite with LoggableEncoder.ToLoggableEncoderOps {
  implicit val arbitrarySymbol: Arbitrary[Symbol] = Arbitrary(Arbitrary.arbitrary[String].map(Symbol(_)))

  def checkFor[A1: LoggableEncoder: ClassTag](createExpectedResult: A1 => LoggableValue)(implicit
      a1: Arbitrary[A1],
      s1: Shrink[A1],
      pp1: A1 => Pretty,
      loc: Location
  ) = {
    val name = implicitly[ClassTag[A1]].runtimeClass.getSimpleName
    property(s"LoggableEncoder should be able to encode $name") {
      forAll { (n: A1) =>
        assertEquals(n.toLoggable, createExpectedResult(n))
      }
    }
  }

  checkFor[Int](n => LoggableIntegral(n.toLong))
  checkFor[Long](n => LoggableIntegral(n))
  checkFor[Short](n => LoggableIntegral(n.toLong))
  checkFor[Byte](n => LoggableIntegral(n.toLong))
  checkFor[Float](n => LoggableFloating(n.toDouble))
  checkFor[Double](n => LoggableFloating(n))
  checkFor[Boolean](b => LoggableBoolean(b))
  checkFor[String](s => LoggableString(s))
  checkFor[Char](c => LoggableString(c.toString))
  checkFor[Symbol](c => LoggableString(c.name))
  checkFor[List[Int]](l => LoggableList(l.map(_.toLoggable)))
  checkFor[Set[Int]](s => LoggableList(s.toList.map(_.toLoggable)))
  checkFor[Map[String, Int]](m => LoggableObject(m.map { case (k, v) => (k, v.toLoggable) }))
  checkFor[Option[Int]](o => o.map(_.toLoggable).getOrElse(LoggableNil))
  checkFor[Either[String, Int]](e => e.fold(_.toLoggable, _.toLoggable))
  checkFor[Vector[Int]](v => LoggableList(v.map(_.toLoggable).toList))
  checkFor[Instant](i => LoggableString(i.toString))
  checkFor[LocalDate](ld => LoggableString(ld.toString))
  checkFor[LocalTime](lt => LoggableString(lt.toString))
  checkFor[LocalDateTime](ldt => LoggableString(ldt.toString))
  checkFor[ZonedDateTime](zdt => LoggableString(zdt.toString))
  checkFor[OffsetDateTime](odt => LoggableString(odt.toString))
  checkFor[OffsetTime](ot => LoggableString(ot.toString))
  checkFor[java.time.Duration](d => LoggableString(d.toString))
  checkFor[scala.concurrent.duration.Duration](d => LoggableString(d.toString))
  checkFor[scala.concurrent.duration.FiniteDuration](d => LoggableString(d.toString))

  test("LoggableEncoder should generate LoggableEncoder instance for simple case class") {
    case class Simple(i: Int)

    val encoder: LoggableEncoder[Simple] = LoggableEncoder.derived

    val simple = Simple(42)

    assertEquals(encoder.toLoggable(simple), LoggableObject("i" -> LoggableIntegral(42)))
  }

  test("LoggableEncoder should generate LoggableEncoder instance for nested case class") {
    case class Simple(i: Int)
    case class Nested(s: Simple)

    @nowarn
    implicit val simpleEncoder: LoggableEncoder[Simple] = LoggableEncoder.derived
    val nestedEncoder: LoggableEncoder[Nested]          = LoggableEncoder.derived

    val nested = Nested(Simple(42))

    assertEquals(nestedEncoder.toLoggable(nested), LoggableObject("s" -> LoggableObject("i" -> LoggableIntegral(42))))
  }

  test("LoggableEncoder should generate LoggableEncoder instance for ADT") {
    sealed trait T
    case class A(x: Int)    extends T
    case class B(y: String) extends T

    implicit val encoder: LoggableEncoder[T] = LoggableEncoder.derived

    assertEquals(encoder.toLoggable(A(42)), LoggableObject("x" -> LoggableIntegral(42)))
    assertEquals(encoder.toLoggable(B("42")), LoggableObject("y" -> LoggableString("42")))
  }

  test("LoggableEncoder should generate LoggableEncoder instance for recursive GADT") {
    sealed trait Tree[A]
    case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]
    case class Leaf[A](elem: A)                       extends Tree[A]

    implicit lazy val encoder: LoggableEncoder[Tree[Int]] = LoggableEncoder.derived

    val tree: Tree[Int] = Node(Leaf(0), Node(Leaf(1), Leaf(2)))

    assertEquals(
      encoder.toLoggable(tree),
      LoggableObject(
        "left" -> LoggableObject("elem" -> LoggableIntegral(0)),
        "right" -> LoggableObject(
          "left"  -> LoggableObject("elem" -> LoggableIntegral(1)),
          "right" -> LoggableObject("elem" -> LoggableIntegral(2))
        )
      )
    )
  }

  test("LoggableEncoder should handle null for simple case class") {
    case class Simple(i: Int)

    implicit val encoder: LoggableEncoder[Simple] = LoggableEncoder.derived

    assertEquals(encoder.toLoggable(null), LoggableNil)
  }

  test("LoggableEncoder should handle null for nested case class") {
    case class Simple(i: Int)
    case class Nested(s: Simple)

    @nowarn
    implicit val simpleEncoder: LoggableEncoder[Simple] = LoggableEncoder.derived
    val nestedEncoder: LoggableEncoder[Nested]          = LoggableEncoder.derived

    val nested = Nested(null)

    assertEquals(nestedEncoder.toLoggable(nested), LoggableObject("s" -> LoggableNil))
  }

  test("LoggableEncoder should handle null for ADT") {
    sealed trait T
    case class A(x: Int)    extends T
    case class B(y: String) extends T

    implicit val encoder: LoggableEncoder[T] = LoggableEncoder.derived

    assertEquals(encoder.toLoggable(null), LoggableNil)
  }

  test("LoggableEncoder should handle null for recursive GADT") {
    sealed trait Tree[A]
    case class Node[A](left: Tree[A], right: Tree[A]) extends Tree[A]
    case class Leaf[A](elem: A)                       extends Tree[A]

    implicit lazy val encoder: LoggableEncoder[Tree[Int]] = LoggableEncoder.derived

    val tree: Tree[Int] = Node(null, Node(Leaf(1), null))

    assertEquals(
      encoder.toLoggable(tree),
      LoggableObject(
        "left" -> LoggableNil,
        "right" -> LoggableObject(
          "left"  -> LoggableObject("elem" -> LoggableIntegral(1)),
          "right" -> LoggableNil
        )
      )
    )
  }
}
