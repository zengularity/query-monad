package utils

import cats.{Applicative, Functor, Monad}

/**
  * @param F the structure we add to our stack
  */
case class ReaderT[F[_], -A, +B](f: A => F[B]) {
  def map[C](g: B => C)(implicit F: Functor[F]): ReaderT[F, A, C] =
    ReaderT(a => F.map(f(a))(g))

  def flatMap[C](g: B => ReaderT[F, A, C])(
      implicit M: Monad[F]): ReaderT[F, A, C] =
    ReaderT(a => M.flatMap(f(a))(b => g(b).f(a)))
}

object ReaderT {
  def pure[F[_], C, A](a: A)(implicit F: Applicative[F]): ReaderT[F, C, A] =
    ReaderT(_ => F.pure(a))

  def ask[F[_], A](implicit F: Applicative[F]): ReaderT[F, A, A] =
    ReaderT(a => F.pure(a))
}
