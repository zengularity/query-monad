package com.zengularity.querymonad.core.database

//import scala.concurrent.Future
import scala.language.higherKinds

/**
 * A class that can run a Query.
 */
sealed trait QueryRunner[F[_], Resource] {
  def apply[T](query: QueryT[F, Resource, T])(
      implicit compose: ComposeWithCompletion[F, T]
  ): F[compose.Outer]
}

object QueryRunner {
  private class DefaultRunner[F[_], Resource](
      wr: WithResource[F, Resource]
  ) extends QueryRunner[F, Resource] {

    def apply[T](
        query: QueryT[F, Resource, T]
    )(implicit compose: ComposeWithCompletion[F, T]): F[compose.Outer] =
      compose(wr, query.run)
  }

  // Default factory
  def apply[F[_], Resource](
      wr: WithResource[F, Resource]
  ): QueryRunner[F, Resource] =
    new DefaultRunner(wr)
}
