package com.zengularity.querymonad.test.core.module.sql.utils

import java.sql.Connection

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.jdbc.{
  AcolyteDSL,
  QueryResult => AcolyteQueryResult,
  ScalaCompositeHandler
}

import com.zengularity.querymonad.core.module.sql.WithSqlConnection

object SqlConnectionFactory {

  def withSqlConnection[A <: AcolyteQueryResult](
      resultsSet: A): WithSqlConnection =
    new WithSqlConnection {
      def apply[B](f: Connection => B): B =
        AcolyteDSL.withQueryResult(resultsSet)(f)

      def async[B](f: Connection => Future[B]): Future[B] =
        AcolyteDSL.withQueryResult(resultsSet) { connection =>
          val result = f(connection)
          result.onComplete(_ => connection.close())
          result
        }
    }

  def withSqlConnection(handler: ScalaCompositeHandler): WithSqlConnection =
    new WithSqlConnection {
      def apply[B](f: Connection => B): B = {
        val con = AcolyteDSL.connection(handler)
        f(con)
      }

      def async[B](f: Connection => Future[B]): Future[B] = {
        val connection = AcolyteDSL.connection(handler)
        val result = f(connection)
        result.onComplete(_ => connection.close())
        result
      }
    }

}
