package com.zengularity.querymonad.core.database

import cats.instances.option.catsStdInstancesForOption

object QueryO {
  def apply[Resource, A](
      run: Resource => Option[A]
  ): QueryO[Resource, A] =
    QueryT.apply[Option, Resource, A](run)

  def pure[Resource, A](a: A) =
    QueryT.pure[Option, Resource, A](a)

  def ask[Resource] =
    QueryT.ask[Option, Resource]

  def liftF[Resource, A](option: Option[A]) =
    QueryT.liftF[Option, Resource, A](option)

  def lift[Resource, A](
      query: Query[Resource, A]
  ) =
    QueryO[Resource, A](query.map(catsStdInstancesForOption.pure).run)

  def fromQuery[Resource, A](
      query: Query[Resource, Option[A]]
  ): QueryO[Resource, A] =
    QueryO[Resource, A](query.run)

  def liftQuery[Resource, A](
      query: Query[Resource, A]
  ): QueryO[Resource, A] =
    QueryO.fromQuery[Resource, A](
      query.map(catsStdInstancesForOption.pure)
    )
}
