package com.zengularity.querymonad.core.database

import scala.concurrent.Future

trait WithResource[Resource] {
  def apply[A](f: Resource => Future[A]): Future[A]
}
