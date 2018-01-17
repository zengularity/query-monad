package core.database

import javax.sql.DataSource

import scala.concurrent.{ExecutionContext, Future}

class QueryRunner(
    db: Database,
    ec: ExecutionContext
) {
  def run[A](query: Query[A]): Future[A] =
    Future {
      db.withConnection(query.f)
    }(ec)

  // TODO: Remove with a transaction run at context level
  def commit[A](query: Query[A]): Future[A] =
    Future {
      db.withTransaction(query.f)
    }(ec)
}

object QueryRunner {
  def apply(db: Database, ec: ExecutionContext) =
    new QueryRunner(db, ec)

  def apply(ds: DataSource, ec: ExecutionContext) =
    new QueryRunner(Database(ds), ec)
}
