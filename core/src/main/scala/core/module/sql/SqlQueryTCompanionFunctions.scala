package com.zengularity.querymonad.core.module.sql

import java.sql.Connection

import scala.language.higherKinds

import cats.Applicative

import com.zengularity.querymonad.core.database.QueryT

trait SqlQueryTCompanionFunctions {
  def apply[M[_], A](run: Connection => M[A]) =
    QueryT.apply[M, Connection, A](run)

  def pure[M[_]: Applicative, A](a: A) =
    QueryT.pure[M, Connection, A](a)

  def ask[M[_]: Applicative] = QueryT.ask[M, Connection]

  def liftF[M[_], A](ma: M[A]) = QueryT.liftF[M, Connection, A](ma)

  def fromQuery[M[_], A](query: SqlQuery[M[A]]) =
    QueryT.fromQuery[M, Connection, A](query)
}
