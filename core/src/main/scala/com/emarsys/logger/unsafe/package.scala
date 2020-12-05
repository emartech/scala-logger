package com.emarsys.logger

package object unsafe {
  object syntax extends UnsafeSyntax

  object instances extends UnsafeInstances

  object implicits extends UnsafeSyntax with UnsafeInstances
}
