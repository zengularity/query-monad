package com.zengularity.querymonad.core.module

import java.sql.Connection

import scala.concurrent.ExecutionContext

import com.zengularity.querymonad.core.database.{
  Query,
  QueryRunner,
  WithResource
}

package object sql {
  type SqlQuery[A] = Query[Connection, A]

  object SqlQuery {
    def pure[A](a: A) = Query.pure[Connection, A](a)

    val ask = Query.ask[Connection]

    def apply[A](f: Connection => A) = new SqlQuery(f)
  }

  type WithSqlConnection = WithResource[Connection]

  type SqlQueryRunner = QueryRunner[Connection]

  object SqlQueryRunner {
    def apply(wc: WithSqlConnection)(
        implicit ec: ExecutionContext): SqlQueryRunner =
      QueryRunner[Connection](wc)
  }
}
