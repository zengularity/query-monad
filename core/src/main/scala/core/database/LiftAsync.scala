package com.zengularity.querymonad.core.database

// import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/**
 * Heavily inspired from work done by @cchantep in Acolyte (see acolyte.reactivemongo.ComposeWithCompletion)
 */
trait LiftAsync[F[_], M[_], A] {
  type Outer

  def apply[Resource](
      loaner: WithResource[F, Resource],
      f: Resource => M[A]
  ): F[Outer]
}

object LiftAsync {

  type Aux[F[_], M[_], A, B] = LiftAsync[F, M, A] { type Outer = B }

}
