package database

import java.sql.Connection

import cats.{Functor, Monad}

import utils.{Id, Reader, ReaderT}

case class QueryT[F[_], DB, A](query: Query[DB, F[A]]) {

  def map[B](f: A => B)(implicit F: Functor[F]): QueryT[F, DB, B] = {
    val underlying = ReaderT(query.underlying.f).map(f)
    QueryT(query.copy(underlying = underlying))
  }

  def flatMap[B](f: A => QueryT[F, DB, B])(
      implicit F: Monad[F]): QueryT[F, DB, B] = {
    val underlying = for {
      a <- ReaderT(query.underlying.f)
      b <- ReaderT(f(a).query.underlying.f)
    } yield b
    QueryT(query.copy(underlying = underlying))
  }

}
