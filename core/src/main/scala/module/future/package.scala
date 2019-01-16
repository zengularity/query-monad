package com.zengularity.querymonad.module

import scala.concurrent.Future

import com.zengularity.querymonad.core.database.{QueryRunner, WithResource}

package object future {

  type WithResourceF[Resource] = WithResource[Future, Resource]

  type QueryRunnerF[Resource] = QueryRunner[Future, Resource]

  object QueryRunnerF {
    def apply[Resource](wc: WithResourceF[Resource]): QueryRunnerF[Resource] =
      QueryRunnerF[Resource](wc)
  }

  object implicits extends LiftAsyncFuture

}
