package com.emarsys.logger.internal

import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.{Matchers, WordSpec}
import shapeless.{HList, LabelledGeneric, Lazy}

class ToMapRecSpec extends WordSpec with TypeCheckedTripleEquals with Matchers {

  def toMapRec[A, L <: HList](a: A)(implicit
                                    gen: LabelledGeneric.Aux[A, L],
                                    toMap: Lazy[ToMapRec[L]]): Map[String, Any] = toMap.value(gen.to(a))

  "ToMapRec" should {
    "convert simple case classes to maps" in {
      case class Simple(i: Int)
      val simple = Simple(0)

      val map = toMapRec(simple)

      map should ===(Map("i" -> 0))
    }

    "convert nested case classes to maps" in {
      case class Simple(i: Int)
      case class Nested(simple: Simple)
      val nested = Nested(Simple(0))

      val map = toMapRec(nested)

      map should ===(Map("simple" -> Map("i" -> 0)))
    }

    "convert multiple levels of nested case classes to maps" in {
      case class Simple(i: Int)
      case class Nested1(simple: Simple)
      case class Nested2(nested1: Nested1)
      val nested = Nested2(Nested1(Simple(0)))

      val map = toMapRec(nested)

      map should ===(Map("nested1" -> Map("simple" -> Map("i" -> 0))))
    }

    "convert case classes containing null to maps" in {
      case class Simple(s: String)

      val simple = Simple(null)

      val map = toMapRec(simple)

      map should ===(Map("s" -> null))
    }
  }
}
