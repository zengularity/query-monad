package core.database

import java.sql.Connection
import javax.sql.DataSource

import scala.util.control.ControlThrowable

/**
  * This class is copy from Play DefaultDatabase class.
  * See https://github.com/playframework/playframework/blob/master/framework/src/play-jdbc/src/main/scala/play/api/db/Databases.scala#L94
  *
  * @dataSource managed dataSource used to obtain connection.
  */
class Database(dataSource: DataSource) {

  def getConnection(autocommit: Boolean): Connection = {
    val connection = dataSource.getConnection
    connection.setAutoCommit(autocommit)
    connection
  }

  def withConnection[A](block: Connection => A): A = {
    withConnection(autocommit = true)(block)
  }

  def withConnection[A](autocommit: Boolean)(block: Connection => A): A = {
    val connection = getConnection(autocommit)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  def withTransaction[A](block: Connection => A): A = {
    withConnection(autocommit = false) { connection =>
      try {
        val r = block(connection)
        connection.commit()
        r
      } catch {
        case e: ControlThrowable =>
          connection.commit()
          throw e
        case e: Throwable =>
          connection.rollback()
          throw e
      }
    }
  }

}

object Database {
  def apply(dataSource: DataSource): Database = new Database(dataSource)
}
