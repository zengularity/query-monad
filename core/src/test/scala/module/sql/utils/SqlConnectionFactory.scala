package com.zengularity.querymonad.test.core.module.sql.utils

import java.sql.Connection

import acolyte.jdbc.{AcolyteDSL, QueryResult  => AcolyteQueryResult, ScalaCompositeHandler}

import com.zengularity.querymonad.core.module.sql.WithSqlConnection

object SqlConnectionFactory {

  def withSqlConnection[A <: AcolyteQueryResult](resultsSet: A): WithSqlConnection = 
    new WithSqlConnection {
      def apply[B](f: Connection => B): B = 
        AcolyteDSL.withQueryResult(resultsSet)(f)
    }

  def withSqlConnection(handler: ScalaCompositeHandler): WithSqlConnection =
    new WithSqlConnection {
      def apply[B](f: Connection => B): B = {
        val con = AcolyteDSL.connection(handler)
        f(con)
      }
    }
  
}
