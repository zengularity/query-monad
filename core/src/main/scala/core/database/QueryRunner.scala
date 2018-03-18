package com.zengularity.querymonad.core.database

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

/**
 * A class who can run a Query.
 */
sealed trait QueryRunner[Resource] {
  def apply[M[_], T](
      query: QueryT[M, Resource, T]
  )(
      implicit compose: ComposeWithCompletion[M, T]
  ): compose.Outer
}

object QueryRunner {
  private class DefaultRunner[Resource](
      wr: WithResource[Resource]
  )(
      implicit ec: ExecutionContext
  ) extends QueryRunner[Resource] {

    def apply[M[_], T](
        query: QueryT[M, Resource, T]
    )(
        implicit compose: ComposeWithCompletion[M, T]
    ): compose.Outer = {
      wr { resource =>
        compose(resource, query.run)(wr.releaseIfNecessary)
      }
    }

  }

  // Default factory
  def apply[Resource](
      wr: WithResource[Resource]
  )(
      implicit ec: ExecutionContext
  ): QueryRunner[Resource] =
    new DefaultRunner(wr)
}
