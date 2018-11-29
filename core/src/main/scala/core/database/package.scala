package com.zengularity.querymonad.core

import scala.language.higherKinds

import cats.Applicative

import cats.Id
import cats.data.{Reader, ReaderT}

package object database {
  type Query[Resource, A] = Reader[Resource, A]

  object Query {
    def pure[Resource, A](a: A) = ReaderT.pure[Id, Resource, A](a)

    def ask[Resource] = ReaderT.ask[Id, Resource]

    def apply[Resource, A](f: Resource => A) = new Query(f)
  }

  type QueryT[F[_], Resource, A] = ReaderT[F, Resource, A]

  object QueryT {
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

    def lift[M[_], Resource, A](
        query: Query[Resource, A]
    )(implicit F: Applicative[M]) =
      QueryT[M, Resource, A](query.map(F.pure).run)

    def fromQuery[M[_], Resource, A](
        query: Query[Resource, M[A]]
    ): QueryT[M, Resource, A] =
      QueryT[M, Resource, A](query.run)

    def liftQuery[M[_]: Applicative, Resource, A](
        query: Query[Resource, A]
    ): QueryT[M, Resource, A] =
      QueryT.fromQuery[M, Resource, A](
        query.map(implicitly[Applicative[M]].pure)
      )
  }

  type QueryO[Resource, A] = QueryT[Option, Resource, A]

  type QueryE[Resource, Err, A] =
    QueryT[({ type F[T] = Either[Err, T] })#F, Resource, A]
}
