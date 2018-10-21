package com.zengularity.querymonad.module.playsql.database

import java.sql.Connection

// import com.typesafe.scalalogging.Logger
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import play.api.db.Database

import com.zengularity.querymonad.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database)(implicit ec: ExecutionContext)
    extends WithSqlConnection {

  // val logger = Logger[WithPlayTransaction]

  def apply[A](f: Connection => Future[A]): Future[A] = {
    lazy val connection = db.getConnection(false)
    val result = f(connection).andThen {
      case Success(x) =>
        connection.commit()
        x
      case Failure(_) =>
        // logger.debug(
        //   s"an error occurred when runnning the operation on database: $ex"
        // )
        connection.rollback()
    }
    result.onComplete(_ => connection.close())
    result
  }

}
