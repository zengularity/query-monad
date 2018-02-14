package com.zengularity.querymonad.core

import scala.language.higherKinds

import cats.{Applicative, Id}
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

    def fromQuery[M[_], Resource, A](
        query: Query[Resource, M[A]]
    ): QueryT[M, Resource, A] =
      QueryT[M, Resource, A](query.run)
  }

  type QueryO[Resource, A] = QueryT[Option, Resource, A]

  object QueryO {
    import cats.instances.option._

    def apply[Resource, A](run: Resource => Option[A]) =
      QueryT.apply[Option, Resource, A](run)

    def pure[Resource, A](a: A) =
      QueryT.pure[Option, Resource, A](a)

    def ask[Resource] = QueryT.ask[Option, Resource]

    def liftF[Resource, A](ma: Option[A]) =
      QueryT.liftF[Option, Resource, A](ma)

    def fromQuery[Resource, A](query: Query[Resource, Option[A]]) =
      QueryT.fromQuery[Option, Resource, A](query)
  }

  type QueryE[Resource, Err, A] =
    QueryT[({ type F[T] = Either[Err, T] })#F, Resource, A]

  object QueryE {
    import cats.instances.either._

    def apply[Resource, Err, A](
        run: Resource => Either[Err, A]
    ): QueryE[Resource, Err, A] =
      new QueryE(run)

    def pure[Resource, Err, A](a: A) =
      QueryT.pure[({ type F[B] = Either[Err, B] })#F, Resource, A](a)

    def ask[Resource, Err] =
      QueryT.ask[({ type F[A] = Either[Err, A] })#F, Resource]

    def liftF[Resource, Err, A](ma: Either[Err, A]) =
      QueryT.liftF[({ type F[B] = Either[Err, B] })#F, Resource, A](ma)

    def fromQuery[Resource, Err, A](query: Query[Resource, Either[Err, A]]) =
      QueryT.fromQuery[({ type F[B] = Either[Err, B] })#F, Resource, A](query)
  }
}
