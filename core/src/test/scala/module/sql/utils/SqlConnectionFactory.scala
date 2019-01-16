package com.zengularity.querymonad.test.module.sql.utils

import java.sql.Connection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.jdbc.{
  AcolyteDSL,
  QueryResult => AcolyteQueryResult,
  ScalaCompositeHandler
}

import com.zengularity.querymonad.module.sql.future.WithSqlConnectionF

object SqlConnectionFactory {

  def withSqlConnection[A <: AcolyteQueryResult](
      resultsSet: A
  ): WithSqlConnectionF =
    new WithSqlConnectionF {
      def apply[B](f: Connection => Future[B]): Future[B] =
        AcolyteDSL.withQueryResult(resultsSet) { connection =>
          f(connection).andThen { case _ => connection.close() }
        }

    }

  def withSqlConnection(handler: ScalaCompositeHandler): WithSqlConnectionF =
    new WithSqlConnectionF {
      def apply[B](f: Connection => Future[B]): Future[B] = {
        val con = AcolyteDSL.connection(handler)
        f(con).andThen { case _ => con.close() }
      }
    }

}
