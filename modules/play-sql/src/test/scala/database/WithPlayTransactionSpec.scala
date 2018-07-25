package com.zengularity.querymonad.test.module.playsql.database

import scala.concurrent.Future

import anorm._
import com.zengularity.querymonad.module.sql.{SqlQuery, SqlQueryRunner}
import play.api.db.Databases
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, ForEach}
import org.specs2.execute.{AsResult, Result}

import com.zengularity.querymonad.module.sql.SqlQueryRunner
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction

class WithPlayTransactionSpec(implicit ee: ExecutionEnv)
    extends Specification
    with BeforeAfterAll
    with ForEach[SqlQueryRunner] {

  lazy val database = Databases.inMemory()

  def beforeAll = {
    // db initialization
    database
    // applying evolutions
    Evolutions.cleanupEvolutions(database)
    Evolutions.applyEvolutions(
      database,
      SimpleEvolutionsReader.forDefault(
        Evolution(
          1,
          "create table user (id int primary key, name varchar(255));",
          "drop table user;"
        ),
        Evolution(
          2,
          "create table user_account (id int primary key, balance number, owner_id int);",
          "drop table user_account;"
        ),
        Evolution(
          3,
          "insert into user values (1, 'picsou');",
          "delete from table user where id = 1;"
        ),
        Evolution(
          4,
          "insert into user_account values (1, 10000000, 1);",
          "delete from table user_account where id = 1;"
        )
      )
    )
  }

  def afterAll = {
    database.shutdown()
  }

  /**
   * Clean database, apply evolutions and instanciate runner for test
   */
  def foreach[R: AsResult](test: SqlQueryRunner => R): Result = {
    val withSqlConnection = new WithPlayTransaction(database)
    val runner = SqlQueryRunner(withSqlConnection)
    AsResult(test(runner))
  }

  val parser =
    (SqlParser.str("name") ~ SqlParser.int("balance")).map {
      case name ~ balance => (name, balance)
    }

  "WithPlayTransaction" should {

    "execute a query fetching on a single table" in { runner: SqlQueryRunner =>
      val query = SqlQuery(
        implicit c =>
          SQL"select name from user where id = 1".as(SqlParser.str(1).single)
      )
      val result = runner(query)
      result must beTypedEqualTo("picsou").await
    }

    "execute a query with a joint" in { runner: SqlQueryRunner =>
      val query = SqlQuery(
        implicit c =>
          SQL"select name, balance from user u join user_account c on u.id = c.owner_id where u.id = 1"
            .as(parser.single)
      )
      val result = runner(query)
      result must beTypedEqualTo(("picsou", 10000000)).await
    }

    "execute a query which insert into 2 tables" in { runner: SqlQueryRunner =>
      val insertQuery = for {
        _ <- SqlQuery(
          implicit c =>
            SQL"insert into user values (2, 'donald')".executeInsert()
        )
        _ <- SqlQuery(
          implicit c =>
            SQL"insert into user_account values (2, 100, 2)".executeInsert()
        )
      } yield ()

      runner(insertQuery) must beTypedEqualTo(()).await

      val getQuery = SqlQuery(
        implicit c =>
          SQL"select name, balance from user u join user_account c on u.id = c.owner_id where u.id = 2"
            .as(parser.single)
      )

      runner(getQuery) must beTypedEqualTo(("donald", 100)).await
    }

    "fail when inserting into a non existent table" in {
      runner: SqlQueryRunner =>
        val query = SqlQuery(
          implicit c =>
            SQL"insert into fake_table values (2, 100, 2)".executeInsert()
        ).map(_ => ())
        Future(runner(query)).flatten must throwA[org.h2.jdbc.JdbcSQLException](
          "Table \"FAKE_TABLE\" not found"
        ).await
    }

    "not insert data when the table doesn't exist" in {
      runner: SqlQueryRunner =>
        val query = for {
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into user values (3, 'daisy')".executeInsert()
          )
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into fake_table values (3, 100, 3)".executeInsert()
          )
        } yield ()
        val queryResult = Future(runner(query))
        val result = queryResult
          .flatMap(
            _ => Future.failed(new RuntimeException("This should never happen"))
          )
          .recoverWith {
            case _ =>
              runner(
                SqlQuery(
                  implicit c =>
                    SQL"select id from user where id = 3"
                      .as(SqlParser.int(1).singleOpt)
                )
              )
          }
        queryResult.flatten must throwA[org.h2.jdbc.JdbcSQLException](
          "Table \"FAKE_TABLE\" not found"
        ).await
        result must beTypedEqualTo(None: Option[Int]).await
    }

  }

}
