package com.zengularity.querymonad.core.database

import scala.concurrent.{ExecutionContext, Future}

/**
  * A class who can run a Query.
  */
sealed trait QueryRunner[Resource] {
  def apply[T](query: Query[Resource, T]): Future[T]
}

object QueryRunner {
  private class DefaultRunner[Resource](wr: WithResource[Resource])(
      implicit ec: ExecutionContext)
      extends QueryRunner[Resource] {
    def apply[T](query: Query[Resource, T]): Future[T] =
      Future(wr(query.run))
  }

  // Default factory
  def apply[Resource](wr: WithResource[Resource])(
      implicit ec: ExecutionContext): QueryRunner[Resource] =
    new DefaultRunner(wr)
}
