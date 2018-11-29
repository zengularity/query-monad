package com.zengularity.querymonad.module.sql

import java.sql.Connection

import com.zengularity.querymonad.core.database.QueryList

object SqlQueryList {
  def apply[A](run: Connection => List[A]) =
    QueryList.apply[Connection, A](run)

  def pure[A](a: A) =
    QueryList.pure[Connection, A](a)

  def ask = QueryList.ask[Connection]

  def liftF[A](list: List[A]) = QueryList.liftF[Connection, A](list)

  def lift[A](query: SqlQuery[A]) =
    QueryList.lift[Connection, A](query)

  def fromQuery[A](query: SqlQuery[List[A]]) =
    QueryList.fromQuery[Connection, A](query)

  def liftQuery[A](query: SqlQuery[A]) =
    QueryList.liftQuery[Connection, A](query)
}
