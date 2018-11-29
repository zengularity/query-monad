package com.zengularity.querymonad.core

import scala.language.higherKinds
import cats.Applicative
import cats.data.{Kleisli, Reader, ReaderT}

package object database {
  type Query[Resource, A] = Reader[Resource, A]

  object Query {
    def pure[F[_]: Applicative, Resource, A](a: A): Kleisli[F, Resource, A] =
      ReaderT.pure[F, Resource, A](a)

    def ask[F[_]: Applicative, Resource]: Kleisli[F, Resource, Resource] =
      ReaderT.ask[F, Resource]

    def apply[Resource, A](f: Resource => A) =
      new Query(f)
  }

  type QueryT[F[_], Resource, A] = ReaderT[F, Resource, A]

  object QueryT {
    def apply[F[_], Resource, A](
        run: Resource => F[A]
    ): QueryT[F, Resource, A] =
      new QueryT(run)

    def pure[F[_]: Applicative, Resource, A](a: A): Kleisli[F, Resource, A] =
      ReaderT.pure[F, Resource, A](a)

    def ask[F[_]: Applicative, Resource]: Kleisli[F, Resource, Resource] =
      ReaderT.ask[F, Resource]

    def liftF[F[_], Resource, A](ma: F[A]): Kleisli[F, Resource, A] =
      ReaderT.liftF[F, Resource, A](ma)

    def lift[F[_], Resource, A](
        query: Query[Resource, A]
    )(implicit F: Applicative[F]) =
      QueryT[F, Resource, A](query.map(F.pure).run)

    def fromQuery[F[_], Resource, A](
        query: Query[Resource, F[A]]
    ): QueryT[F, Resource, A] =
      QueryT[F, Resource, A](query.run)

    def liftQuery[F[_]: Applicative, Resource, A](
        query: Query[Resource, A]
    ): QueryT[F, Resource, A] =
      QueryT.fromQuery[F, Resource, A](
        query.map(implicitly[Applicative[F]].pure)
      )
  }

  type QueryO[Resource, A] = QueryT[Option, Resource, A]

  type QueryE[Resource, Err, A] =
    QueryT[({ type F[T] = Either[Err, T] })#F, Resource, A]
}
