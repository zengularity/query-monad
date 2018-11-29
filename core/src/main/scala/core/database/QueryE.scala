package com.zengularity.querymonad.core.database

import cats.instances.either.catsStdInstancesForEither

object QueryE {
  def apply[Resource, Err, A](
      run: Resource => Either[Err, A]
  ): QueryE[Resource, Err, A] =
    QueryT.apply[Either[Err, ?], Resource, A](run)

  def pure[Resource, Err, A](
      a: A
  ) =
    QueryT.pure[Either[Err, ?], Resource, A](a)

  def ask[Resource, Err] =
    QueryT.ask[Either[Err, ?], Resource]

  def liftF[Resource, Err, A](either: Either[Err, A]) =
    QueryT.liftF[Either[Err, ?], Resource, A](either)

  def lift[Resource, Err, A](
      query: Query[Resource, A]
  ) =
    QueryE[Resource, Err, A](query.map(catsStdInstancesForEither.pure).run)

  def fromQuery[Resource, Err, A](
      query: Query[Resource, Either[Err, A]]
  ): QueryE[Resource, Err, A] =
    QueryE[Resource, Err, A](query.run)

  def liftQuery[Resource, Err, A](
      query: Query[Resource, A]
  ): QueryE[Resource, Err, A] =
    QueryE.fromQuery[Resource, Err, A](
      query.map(catsStdInstancesForEither.pure)
    )
}
