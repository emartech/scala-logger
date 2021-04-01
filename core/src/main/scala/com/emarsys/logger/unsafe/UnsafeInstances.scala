package com.emarsys.logger.unsafe

import cats.{Applicative, Id}
import com.emarsys.logger.Logging

trait UnsafeInstances extends UnsafeIdLoggingInstance

private [unsafe] trait UnsafeIdLoggingInstance extends UnsafeApplicativeInstance {
  implicit lazy val defaultUnsafeLoggingInstance: Logging[Id] = Logging.createUnsafeLogger("default")
}

private [unsafe] trait UnsafeApplicativeInstance {
  import cats.syntax.applicative._

  implicit def applicativeLoggingInstance[F[_]: Applicative](implicit underlying: Logging[Id]): Logging[F] =
    Logging.create { (level, msg, ctx) =>
      underlying.log(level, msg, ctx).pure[F]
    }
}
