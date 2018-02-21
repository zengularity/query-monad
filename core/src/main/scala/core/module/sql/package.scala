package com.zengularity.querymonad.core.module

import java.sql.Connection

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

import com.zengularity.querymonad.core.database.{
  Query,
  QueryRunner,
  QueryT,
  QueryO,
  QueryE,
  WithResource
}

package object sql {

  // Query aliases
  type SqlQuery[A] = Query[Connection, A]

  object SqlQuery {
    def pure[A](a: A) = Query.pure[Connection, A](a)

    val ask = Query.ask[Connection]

    def apply[A](f: Connection => A) = new SqlQuery(f)
  }

  // Query transformer aliases
  type SqlQueryT[F[_], A] = QueryT[F, Connection, A]

  object SqlQueryT extends SqlQueryTCompanionFunctions

  type SqlQueryO[A] = QueryO[Connection, A]

  object SqlQueryO extends SqlQueryTCompanionFunctions

  type SqlQueryE[A, Err] = QueryE[Connection, A, Err]

  object SqlQueryE extends SqlQueryTCompanionFunctions

  // Query runner aliases
  type WithSqlConnection = WithResource[Connection]

  type SqlQueryRunner = QueryRunner[Connection]

  object SqlQueryRunner {
    def apply(wc: WithSqlConnection)(
        implicit ec: ExecutionContext): SqlQueryRunner =
      QueryRunner[Connection](wc)
  }

}
