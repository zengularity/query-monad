package com.zengularity.querymonad.module.catsio

import cats.effect.IO
import com.zengularity.querymonad.module.sql.{SqlQueryRunner, WithSqlConnection}

package object sql {

  type WithSqlConnectionIO = WithSqlConnection[IO]

  type SqlQueryRunnerIO = SqlQueryRunner[IO]

  object SqlQueryRunnerIO {
    def apply(wc: WithSqlConnectionIO): SqlQueryRunnerIO =
      SqlQueryRunner(wc)
  }

}
