package com.zengularity.querymonad.core.module

import java.sql.Connection

import scala.concurrent.ExecutionContext
import scala.language.higherKinds

import cats.Applicative

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

  object SqlQueryT {
    def apply[M[_], A](run: Connection => M[A]) =
      QueryT.apply[M, Connection, A](run)

    def pure[M[_]: Applicative, A](a: A) =
      QueryT.pure[M, Connection, A](a)

    def ask[M[_]: Applicative] = QueryT.ask[M, Connection]

    def liftF[M[_], A](ma: M[A]) = QueryT.liftF[M, Connection, A](ma)

    def fromQuery[M[_], A](query: SqlQuery[M[A]]) =
      QueryT.fromQuery[M, Connection, A](query)
  }

  type SqlQueryO[A] = QueryO[Connection, A]

  object SqlQueryO {
    def apply[A](run: Connection => Option[A]) =
      QueryO.apply[Connection, A](run)

    def pure[A](a: A) =
      QueryO.pure[Connection, A](a)

    val ask = QueryO.ask[Connection]

    def liftF[A](ma: Option[A]) =
      QueryO.liftF[Connection, A](ma)

    def fromQuery[A](query: SqlQuery[Option[A]]) =
      QueryO.fromQuery[Connection, A](query)
  }

  type SqlQueryE[A, Err] = QueryE[Connection, A, Err]

  object SqlQueryE {
    def apply[Err, A](run: Connection => Either[Err, A]) =
      QueryE.apply[Connection, Err, A](run)

    def pure[Err, A](a: A) =
      QueryE.pure[Connection, Err, A](a)

    def ask[Err] = QueryE.ask[Connection, Err]

    def liftF[Err, A](ma: Either[Err, A]) =
      QueryE.liftF[Connection, Err, A](ma)

    def fromQuery[Err, A](query: SqlQuery[Either[Err, A]]) =
      QueryE.fromQuery[Connection, Err, A](query)
  }

  // Query runner aliases
  type WithSqlConnection = WithResource[Connection]

  type SqlQueryRunner = QueryRunner[Connection]

  object SqlQueryRunner {
    def apply(wc: WithSqlConnection)(
        implicit ec: ExecutionContext): SqlQueryRunner =
      QueryRunner[Connection](wc)
  }

}
