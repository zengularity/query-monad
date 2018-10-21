package com.zengularity.querymonad.core.database

import scala.concurrent.Future
import scala.language.higherKinds

/**
 * A class who can run a Query.
 */
sealed trait QueryRunner[Resource] {
  def apply[M[_], T](query: QueryT[M, Resource, T])(
      implicit compose: ComposeWithCompletion[M, T]
  ): Future[compose.Outer]
}

object QueryRunner {
  private class DefaultRunner[Resource](wr: WithResource[Resource])
      extends QueryRunner[Resource] {
    def apply[M[_], T](
        query: QueryT[M, Resource, T]
    )(implicit compose: ComposeWithCompletion[M, T]): Future[compose.Outer] =
      compose(wr, query.run)
  }

  // Default factory
  def apply[Resource](
      wr: WithResource[Resource]
  ): QueryRunner[Resource] =
    new DefaultRunner(wr)
}
