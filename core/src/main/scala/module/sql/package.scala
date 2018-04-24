package com.zengularity.querymonad.module

import java.sql.Connection

// import scala.concurrent.ExecutionContext
import scala.language.higherKinds

import cats.Applicative

import com.zengularity.querymonad.core.database.{
  Query,
  QueryE,
  QueryO,
  QueryRunner,
  QueryT,
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

  object SqlQueryT {
    def apply[M[_], A](run: Connection => M[A]) =
      QueryT.apply[M, Connection, A](run)

    def pure[M[_]: Applicative, A](a: A) =
      QueryT.pure[M, Connection, A](a)

    def ask[M[_]: Applicative] = QueryT.ask[M, Connection]

    def liftF[M[_], A](ma: M[A]) = QueryT.liftF[M, Connection, A](ma)

    def lift[M[_], A](
        query: SqlQuery[A]
    )(implicit F: Applicative[M]) =
      SqlQueryT[M, A](query.map(F.pure).run)

    def fromQuery[M[_], A](query: SqlQuery[M[A]]) =
      QueryT.fromQuery[M, Connection, A](query)

    def liftQuery[M[_]: Applicative, A](query: SqlQuery[A]) =
      QueryT.liftQuery[M, Connection, A](query)
  }

  type SqlQueryO[A] = QueryO[Connection, A]

  type SqlQueryE[A, Err] = QueryE[Connection, A, Err]

  // Query runner aliases
  type WithSqlConnection = WithResource[Connection]

  type SqlQueryRunner = QueryRunner[Connection]

  object SqlQueryRunner {
    def apply(wc: WithSqlConnection): SqlQueryRunner =
      QueryRunner[Connection](wc)
  }

}
