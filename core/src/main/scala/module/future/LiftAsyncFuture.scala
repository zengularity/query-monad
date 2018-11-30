package com.zengularity.querymonad.module.future

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

import com.zengularity.querymonad.core.database.LiftAsync

trait LiftAsyncFuture extends LowPriority {

  implicit def futureOut[A]: LiftAsync.Aux[Future, Future, A, A] =
    new LiftAsync[Future, Future, A] {
      type Outer = A

      def apply[In](
          loaner: WithResourceF[In],
          f: In => Future[A]
      ): Future[Outer] = loaner(f)

      override val toString = "futureOut"
    }

}

trait LowPriority { _: LiftAsyncFuture =>

  implicit def pureOut[F[_], A](
      implicit ec: ExecutionContext
  ): LiftAsync.Aux[Future, F, A, F[A]] =
    new LiftAsync[Future, F, A] {
      type Outer = F[A]

      def apply[In](loaner: WithResourceF[In], f: In => F[A]): Future[Outer] =
        loaner(r => Future(f(r)))

      override val toString = "pureOut"
    }

}
