package com.zengularity.querymonad.core.database

trait WithResource[Resource] {
  def apply[A](f: Resource => A): A
}
