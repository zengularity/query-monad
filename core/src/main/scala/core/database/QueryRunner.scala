package com.zengularity.querymonad.core.database

import scala.concurrent.{ExecutionContext, Future}

/**
  * A class who can run a Query.
  */
sealed trait QueryRunner {
  def apply[T](query: Query[T]): Future[T]
}

object QueryRunner {
  private class DefaultRunner(wc: WithConnection)(implicit ec: ExecutionContext)
      extends QueryRunner {
    def apply[T](query: Query[T]): Future[T] =
      Future(wc(query.run))
  }

  // Default factory
  def apply(wc: WithConnection)(implicit ec: ExecutionContext): QueryRunner =
    new DefaultRunner(wc)
}
