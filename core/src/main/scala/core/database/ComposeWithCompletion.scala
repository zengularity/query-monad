package com.zengularity.querymonad.core.database

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/**
 * Heavily inspired from work done by @cchantep in Acolyte (see acolyte.reactivemongo.ComposeWithCompletion)
 */
trait ComposeWithCompletion[F[_], Out] {
  type Outer

  def apply[In](loaner: WithResource[In], f: In => F[Out]): Future[Outer]
}

object ComposeWithCompletion extends LowPriorityCompose {

  type Aux[F[_], A, B] = ComposeWithCompletion[F, A] { type Outer = B }

  implicit def futureOut[A]: Aux[Future, A, A] =
    new ComposeWithCompletion[Future, A] {
      type Outer = A

      def apply[In](loaner: WithResource[In],
                    f: In => Future[A]): Future[Outer] = loaner(f)

      override val toString = "futureOut"
    }

}

trait LowPriorityCompose { _: ComposeWithCompletion.type =>

  implicit def pureOut[F[_], A](
      implicit ec: ExecutionContext
  ): Aux[F, A, F[A]] =
    new ComposeWithCompletion[F, A] {
      type Outer = F[A]

      def apply[In](loaner: WithResource[In], f: In => F[A]): Future[Outer] =
        loaner(r => Future(f(r)))

      override val toString = "pureOut"
    }

}
