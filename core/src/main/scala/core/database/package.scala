package com.zengularity.querymonad.core

import java.sql.Connection

import scala.language.higherKinds

import cats.{Applicative, Id}
import cats.data.{Reader, ReaderT}

package object database {
  type Query[A] = Reader[Connection, A]

  object Query {
    def pure[A](a: A) = ReaderT.pure[Id, Connection, A](a)

    val ask = ReaderT.ask[Id, Connection]

    def apply[A](f: Connection => A) = new Query(f)
  }

  type QueryT[F[_], A] = ReaderT[F, Connection, A]

  object QueryT {
    def pure[M[_]: Applicative, A](a: A) = ReaderT.pure[M, Connection, A](a)

    def ask[M[_]: Applicative] = ReaderT.ask[M, Connection]

    def liftF[M[_], A](ma: M[A]) = ReaderT.liftF[M, Connection, A](ma)
  }

  type QueryO[A] = QueryT[Option, A]

  type QueryE[A, Err] = QueryT[({ type Res[T] = Either[Err, T] })#Res, A]
}
