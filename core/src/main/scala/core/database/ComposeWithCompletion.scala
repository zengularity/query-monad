package com.zengularity.querymonad.core.database

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds

/**
 * Heavily inspired from work done by @cchantep in Acolyte (see acolyte.reactivemongo.ComposeWithCompletion)
 */
trait ComposeWithCompletion[F[_], Out] {
  type Outer <: Future[_]

  def apply[In](resource: In, f: In => F[Out])(onComplete: In => Unit)(
      implicit ec: ExecutionContext): Outer
}

object ComposeWithCompletion extends LowPriorityCompose {

  type Aux[F[_], A, B] = ComposeWithCompletion[F, A] { type Outer = Future[B] }

  implicit def futureOut[A]: Aux[Future, A, A] =
    new ComposeWithCompletion[Future, A] {
      type Outer = Future[A]

      def apply[In](resource: In, f: In => Future[A])(onComplete: In => Unit)(
          implicit ec: ExecutionContext): Outer =
        f(resource).andThen {
          case _ => onComplete(resource)
        }

      override val toString = "futureOut"
    }

}

trait LowPriorityCompose { _: ComposeWithCompletion.type =>

  implicit def pureOut[F[_], A]: Aux[F, A, F[A]] =
    new ComposeWithCompletion[F, A] {
      type Outer = Future[F[A]]

      def apply[In](resource: In, f: In => F[A])(onComplete: In => Unit)(
          implicit ec: ExecutionContext): Outer =
        Future(f(resource)).andThen { case _ => onComplete(resource) }

      override val toString = "pureOut"
    }

}
