package com.emarsys.logger.unsafe

import cats.Id
import com.emarsys.logger.Logging

trait UnsafeSyntax {

  @deprecated("Use com.emarsys.logger.log instead", since = "0.8.0")
  def unsafeLog(implicit logging: Logging[Id]): Logging[Id] = logging

}
