package com.zengularity.querymonad.test.module.catsio.sql.utils

import java.sql.Connection

import acolyte.jdbc.{
  AcolyteDSL,
  ScalaCompositeHandler,
  QueryResult => AcolyteQueryResult
}
import cats.effect.IO
import com.zengularity.querymonad.module.catsio.sql.WithSqlConnectionIO

object SqlConnectionFactory {

  def withSqlConnection[A <: AcolyteQueryResult](
      resultsSet: A
  ): WithSqlConnectionIO =
    new WithSqlConnectionIO {
      def apply[B](f: Connection => IO[B]): IO[B] =
        AcolyteDSL.withQueryResult(resultsSet) { connection =>
          f(connection).guarantee(IO(connection.close()))
        }
    }

  def withSqlConnection(handler: ScalaCompositeHandler): WithSqlConnectionIO =
    new WithSqlConnectionIO {
      def apply[B](f: Connection => IO[B]): IO[B] = {
        val connection = AcolyteDSL.connection(handler)
        f(connection).guarantee(IO(connection.close()))
      }
    }

}
