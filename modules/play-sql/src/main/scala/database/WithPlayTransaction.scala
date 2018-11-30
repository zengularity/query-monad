package com.zengularity.querymonad.module.playsql.database

import java.sql.Connection

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import com.typesafe.scalalogging.Logger
import play.api.db.Database

import com.zengularity.querymonad.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database)(implicit ec: ExecutionContext)
    extends WithSqlConnection[Future] {

  val logger = Logger[WithPlayTransaction]

  def apply[A](f: Connection => Future[A]): Future[A] = {
    lazy val connection = db.getConnection(false)
    f(connection)
      .andThen {
        case Success(x) =>
          connection.commit()
          x
        case Failure(ex) =>
          logger.debug(
            s"an error occurred when runnning the operation on database: $ex"
          )
          connection.rollback()
      }
      .andThen { case _ => connection.close() }
  }

}
