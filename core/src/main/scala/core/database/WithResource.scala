package com.zengularity.querymonad.core.database

trait WithResource[F[_], Resource] {
  def apply[A](f: Resource => F[A]): F[A]
}

//import scala.concurrent.Future
//
//trait WithResource[Resource] {
//  def apply[A](f: Resource => Future[A]): Future[A]
//}
