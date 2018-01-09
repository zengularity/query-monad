package database

import scala.concurrent.{ExecutionContext, Future}

import play.api.db.Database

class QueryRunner[DB <: Database](
    db: Database,
    ec: ExecutionContext
) {
  def run[A](query: Query[A]): Future[A] =
    Future {
      db.withConnection(query.f)
    }(ec)

  def commit[A](query: Query[A]): Future[A] =
    Future {
      db.withTransaction(query.f)
    }(ec)
}

object QueryRunner {
  def apply[DB <: Database](db: Database, ec: ExecutionContext) =
    new QueryRunner(db, ec)
}
