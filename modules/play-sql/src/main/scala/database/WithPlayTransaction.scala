package com.zengularity.querymonad.module.playsql.database

import java.sql.Connection
import scala.util.control.ControlThrowable

import play.api.db.Database

import com.zengularity.querymonad.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database) extends WithSqlConnection {
  def apply[A](f: Connection => A): A = {
    val connection = db.getConnection(false)
    val result = try {
      f(connection)
    } catch {
      case ex: Throwable =>
        connection.rollback()
        throw ex
    }
    result
  }

  def releaseIfNecessary(connection: Connection): Unit = {
    try {
      connection.commit()
    } finally {
      connection.close()
    }
  }
}
