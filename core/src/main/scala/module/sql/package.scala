package com.zengularity.querymonad.module

import java.sql.Connection
import cats.data.Kleisli

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
    def pure[F[_]: Applicative, A](a: A): Kleisli[F, Connection, A] =
      Query.pure[F, Connection, A](a)

    def ask[F[_]: Applicative]: Kleisli[F, Connection, Connection] =
      Query.ask[F, Connection]

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

  // TODO move into SqlQueryRunner companion?
  // Query runner aliases
  type WithSqlConnection[F[_]] = WithResource[F, Connection]

  // java.sql.Connection runner
  type SqlQueryRunner[F[_]] = QueryRunner[F, Connection]

  object SqlQueryRunner {
    def apply[F[_]](wc: WithSqlConnection[F]): SqlQueryRunner[F] =
      QueryRunner[F, Connection](wc)
  }

}
