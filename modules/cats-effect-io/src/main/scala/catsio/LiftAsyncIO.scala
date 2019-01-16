package com.zengularity.querymonad.module.catsio

import scala.language.higherKinds

import cats.effect.{Async, IO}

import com.zengularity.querymonad.core.database.WithResource
import com.zengularity.querymonad.core.database.LiftAsync

trait LiftAsyncIO extends LowPriority {

  implicit def ioOut[F[_]: Async, A]: LiftAsync.Aux[F, F, A, A] =
    new LiftAsync[F, F, A] {
      type Outer = A

      def apply[In](
          loaner: WithResource[F, In],
          f: In => F[A]
      ): F[Outer] = loaner(f)

      override val toString = "ioOut"
    }

}

trait LowPriority { _: LiftAsyncIO =>

  implicit def pureOut[F[_], A]: LiftAsync.Aux[IO, F, A, F[A]] =
    new LiftAsync[IO, F, A] {
      type Outer = F[A]

      def apply[In](loaner: WithResourceIO[In], f: In => F[A]): IO[Outer] =
        loaner(r => IO(f(r)))

      override val toString = "pureOut"
    }

}
