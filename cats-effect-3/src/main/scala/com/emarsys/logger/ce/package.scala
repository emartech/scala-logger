package com.emarsys.logger

import cats.effect.IO

package object ce {
  type LoggedIO[A] = Logged[IO, A]
}
