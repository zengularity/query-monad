package database

import java.sql.Connection
import scala.concurrent.{ExecutionContext, Future}

import play.api.db.Database

import utils.Reader

case class Query[DB <: Database, +A](
    db: DB,
    ec: ExecutionContext,
    underlying: Reader[Connection, A]
) {

  def map[B](g: A => B): Query[DB, B] =
    Query(db, ec, underlying.map(g))

  def flatMap[B](g: A => Query[DB, B]): Query[DB, B] = {
    val gofUnderlying = for {
      a <- underlying
      b <- g(a).underlying
    } yield b
    Query(db, ec, gofUnderlying)
  }

  def run(): Future[A] = {
    Future {
      db.withConnection(underlying.f)
    }(ec)
  }

  def commit(): Future[A] = {
    Future {
      db.withTransaction(underlying.f)
    }(ec)
  }

}
