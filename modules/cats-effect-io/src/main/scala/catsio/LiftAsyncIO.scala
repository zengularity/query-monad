package com.zengularity.querymonad.module.catsio

import scala.language.higherKinds

import cats.effect.IO

import com.zengularity.querymonad.core.database.LiftAsync

trait LiftAsyncIO extends LowPriority {

  implicit def ioOut[A]: LiftAsync.Aux[IO, IO, A, A] =
    new LiftAsync[IO, IO, A] {
      type Outer = A

      def apply[In](
          loaner: WithResourceIO[In],
          f: In => IO[A]
      ): IO[Outer] = loaner(f)

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
