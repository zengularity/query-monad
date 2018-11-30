package com.zengularity.querymonad.core.database

import scala.language.higherKinds

/**
 * A class who can run a Query.
 */
sealed trait QueryRunner[F[_], Resource] {
  def apply[M[_], T](query: QueryT[M, Resource, T])(
      implicit lift: LiftAsync[F, M, T]
  ): F[lift.Outer]
}

object QueryRunner {
  private class DefaultRunner[F[_], Resource](wr: WithResource[F, Resource])
      extends QueryRunner[F, Resource] {
    def apply[M[_], T](
        query: QueryT[M, Resource, T]
    )(implicit lift: LiftAsync[F, M, T]): F[lift.Outer] =
      lift(wr, query.run)
  }

  // Default factory
  def apply[F[_], Resource](
      wr: WithResource[F, Resource]
  ): QueryRunner[F, Resource] =
    new DefaultRunner(wr)
}
