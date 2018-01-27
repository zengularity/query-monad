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
    def pure[M[_]: Applicative, Resource, A](a: A) =
      ReaderT.pure[M, Resource, A](a)

    def ask[M[_]: Applicative, Resource] = ReaderT.ask[M, Resource]

    def liftF[M[_], Resource, A](ma: M[A]) = ReaderT.liftF[M, Resource, A](ma)
  }

  type QueryO[Resource, A] = QueryT[Option, Resource, A]

  type QueryE[Resource, A, Err] =
    QueryT[({ type F[T] = Either[Err, T] })#F, Resource, A]
}
