package com.zengularity.querymonad.core.database

import scala.language.higherKinds

import cats.Applicative
import cats.data.ReaderT

trait QueryTCompanionFunctions {
  def apply[M[_], Resource, A](
      run: Resource => M[A]
  ): QueryT[M, Resource, A] =
    new QueryT(run)

  def pure[M[_]: Applicative, Resource, A](a: A) =
    ReaderT.pure[M, Resource, A](a)

  def ask[M[_]: Applicative, Resource] =
    ReaderT.ask[M, Resource]

  def liftF[M[_], Resource, A](ma: M[A]) =
    ReaderT.liftF[M, Resource, A](ma)

  def fromQuery[M[_], Resource, A](
      query: Query[Resource, M[A]]
  ): QueryT[M, Resource, A] =
    QueryT[M, Resource, A](query.run)
}
