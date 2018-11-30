package com.zengularity.querymonad.module

import cats.effect.IO
import com.zengularity.querymonad.core.database.{QueryRunner, WithResource}

package object catsio {
  type WithResourceIO[Resource] = WithResource[IO, Resource]

  type QueryRunnerIO[Resource] = QueryRunner[IO, Resource]

  object QueryRunnerIO {
    def apply[Resource](wc: WithResourceIO[Resource]): QueryRunnerIO[Resource] =
      QueryRunnerIO[Resource](wc)
  }

  object implicits extends LiftAsyncIO
}
