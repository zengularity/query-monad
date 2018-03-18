package com.zengularity.querymonad.examples.database

import java.sql.Connection

import play.api.db.Database

import com.zengularity.querymonad.core.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database) extends WithSqlConnection {
  def apply[A](f: Connection => A): A = {
    val connection = db.getConnection(true)
    val result     = f(connection)
    result
  }

  def releaseIfNecessary(connection: Connection): Unit = connection.close()
}
