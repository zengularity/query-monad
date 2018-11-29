package com.zengularity.querymonad.module.sql

import java.sql.Connection

import com.zengularity.querymonad.core.database.QueryO

object SqlQueryO {
  def apply[A](run: Connection => Option[A]) =
    QueryO.apply[Connection, A](run)

  def pure[A](a: A) =
    QueryO.pure[Connection, A](a)

  def ask = QueryO.ask[Connection]

  def liftF[A](option: Option[A]) = QueryO.liftF[Connection, A](option)

  def lift[A](query: SqlQuery[A]) =
    QueryO.lift[Connection, A](query)

  def fromQuery[A](query: SqlQuery[Option[A]]) =
    QueryO.fromQuery[Connection, A](query)

  def liftQuery[A](query: SqlQuery[A]) =
    QueryO.liftQuery[Connection, A](query)
}
