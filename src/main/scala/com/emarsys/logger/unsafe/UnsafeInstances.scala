package com.emarsys.logger.unsafe

import cats.{Applicative, Id}
import com.emarsys.logger.Logging

import scala.language.implicitConversions

trait UnsafeInstances extends UnsafeIdLoggingInstance {
  import cats.syntax.applicative._

  implicit def applicativeLoggingInstance[F[_]: Applicative](implicit underlying: Logging[Id]): Logging[F] =
    Logging.create { (level, msg, ctx) =>
      underlying.log(level, msg, ctx).pure[F]
    }
}

trait UnsafeIdLoggingInstance {

  implicit lazy val defaultUnsafeLoggingInstance: Logging[Id] = Logging.createUnsafeLogger("default")

}
