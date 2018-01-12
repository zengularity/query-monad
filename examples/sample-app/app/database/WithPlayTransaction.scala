package com.zengularity.querymonad.examples.database

import java.sql.Connection

import play.api.db.Database

import com.zengularity.querymonad.core.database.WithConnection

class WithPlayTransaction(db: Database) extends WithConnection {
  def apply[A](f: Connection => A): A =
    db.withTransaction(f)
}
