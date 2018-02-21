package com.zengularity.querymonad.core

import scala.language.higherKinds

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

  object QueryT extends QueryTCompanionFunctions

  type QueryO[Resource, A] = QueryT[Option, Resource, A]

  object QueryO extends QueryTCompanionFunctions

  type QueryE[Resource, Err, A] =
    QueryT[({ type F[T] = Either[Err, T] })#F, Resource, A]

  object QueryE extends QueryTCompanionFunctions
}
