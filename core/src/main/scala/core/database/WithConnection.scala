package com.zengularity.querymonad.core.database

import java.sql.Connection

trait WithConnection {
  def apply[A](f: Connection => A): A
}
