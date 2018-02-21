package com.zengularity.querymonad.examples.database

import java.sql.Connection

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.ControlThrowable

import play.api.db.Database

import com.zengularity.querymonad.core.module.sql.WithSqlConnection

class WithPlayTransaction(db: Database)(implicit ec: ExecutionContext)
    extends WithSqlConnection {
  def apply[A](f: Connection => A): A =
    db.withTransaction(f)

  def async[B](f: Connection => Future[B]): Future[B] =
    db.withConnection(false) { connection =>
      val result = f(connection)
      result.onComplete { _ =>
        connection.commit()
        connection.close()
      }
      result.recoverWith {
        case ex: ControlThrowable =>
          connection.commit()
          connection.close()
          Future.failed(ex)
        case ex: Throwable =>
          connection.rollback()
          connection.close()
          Future.failed(ex)
      }
    }
}
