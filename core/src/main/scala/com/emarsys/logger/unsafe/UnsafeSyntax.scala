package com.emarsys.logger.unsafe

import cats.Id
import com.emarsys.logger.Logging

trait UnsafeSyntax {

  def unsafeLog(implicit logging: Logging[Id]): Logging[Id] = logging

}
