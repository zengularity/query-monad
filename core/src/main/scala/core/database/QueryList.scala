package com.zengularity.querymonad.core.database

import cats.instances.list.catsStdInstancesForList

object QueryList {
  def apply[Resource, A](
      run: Resource => List[A]
  ): QueryList[Resource, A] =
    QueryT.apply[List, Resource, A](run)

  def pure[Resource, A](a: A) =
    QueryT.pure[List, Resource, A](a)

  def ask[Resource] =
    QueryT.ask[List, Resource]

  def liftF[Resource, A](list: List[A]) =
    QueryT.liftF[List, Resource, A](list)

  def lift[Resource, A](
      query: Query[Resource, A]
  ) =
    QueryList[Resource, A](query.map(catsStdInstancesForList.pure).run)

  def fromQuery[Resource, A](
      query: Query[Resource, List[A]]
  ): QueryList[Resource, A] =
    QueryList[Resource, A](query.run)

  def liftQuery[Resource, A](
      query: Query[Resource, A]
  ): QueryList[Resource, A] =
    QueryList.fromQuery[Resource, A](
      query.map(catsStdInstancesForList.pure)
    )
}
