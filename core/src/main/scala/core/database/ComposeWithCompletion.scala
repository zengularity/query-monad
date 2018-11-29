package com.zengularity.querymonad.core.database

//import scala.concurrent.Future
import scala.language.higherKinds
import cats.Applicative

/**
 * Heavily inspired from work done by @cchantep in Acolyte (see acolyte.reactivemongo.ComposeWithCompletion)
 */
trait ComposeWithCompletion[F[_], Out] {
  type Outer

  def apply[In](loaner: WithResource[F, In], f: In => F[Out]): F[Outer]
}

object ComposeWithCompletion extends LowPriorityCompose {

  type Aux[F[_], A, B] = ComposeWithCompletion[F, A] { type Outer = B }

//  implicit def futureOut[A]/*(
//    implicit ec: ExecutionContext
//  )*/: Aux[Future, A, A] =
//    new ComposeWithCompletion[Future, A] {
//      type Outer = A
//
//      def apply[In](
//          loaner: WithResource[Future, In],
//          f: In => Future[A]
//      ): Future[Outer] = loaner(f)
//
//      override val toString = "futureOut"
//    }

}

trait LowPriorityCompose { _: ComposeWithCompletion.type =>

  implicit def pureOut[F[_]: Applicative, A]: Aux[F, A, F[A]] =
    new ComposeWithCompletion[F, A] {
      type Outer = F[A]

      def apply[In](loaner: WithResource[F, In], f: In => F[A]): F[Outer] =
        loaner(r => Applicative[F].pure(f(r)))

      override val toString = "pureOut"
    }

}
