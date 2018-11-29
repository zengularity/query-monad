package com.zengularity.querymonad.module.sql

import java.sql.Connection

import com.zengularity.querymonad.core.database.QueryE

object SqlQueryE {
  def apply[Err, A](run: Connection => Either[Err, A]) =
    QueryE.apply[Connection, Err, A](run)

  def pure[Err, A](a: A) =
    QueryE.pure[Connection, Err, A](a)

  def ask[Err] =
    QueryE.ask[Connection, Err]

  def liftF[Err, A](option: Either[Err, A]) =
    QueryE.liftF[Connection, Err, A](option)

  def lift[Err, A](query: SqlQuery[A]) =
    QueryE.lift[Connection, Err, A](query)

  def fromQuery[Err, A](query: SqlQuery[Either[Err, A]]) =
    QueryE.fromQuery[Connection, Err, A](query)

  def liftQuery[Err, A](query: SqlQuery[A]) =
    QueryE.liftQuery[Connection, Err, A](query)
}
