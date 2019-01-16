package com.zengularity.querymonad.core.database

// import scala.concurrent.Future
import scala.language.higherKinds

trait WithResource[F[_], Resource] {
  def apply[A](f: Resource => F[A]): F[A]
}
