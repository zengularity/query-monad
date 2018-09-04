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
  }

  def afterAll = {
    database.shutdown()
  }

  private def applyEvolutions(): Unit = {
    Evolutions.applyEvolutions(
      database,
      SimpleEvolutionsReader.forDefault(
        Evolution(
          1,
          "create table author (id int primary key, author_name varchar(255));",
          "drop table author;"
        ),
        Evolution(
          2,
          "create table book (id int primary key, book_name varchar(255), year int, publisher varchar(255), author_id int);",
          "drop table book;"
        ),
        Evolution(
          3,
          "insert into author values (1, 'Martin Odersky');",
          "delete from table author where id = 1;"
        ),
        Evolution(
          4,
          "insert into book values (1, 'Programming in Scala', 2010, 'O''Reilly', 1);",
          "delete from table book where id = 1;"
        )
      )
    )
  }

  /**
   * Clean database, apply evolutions and instanciate runner for test
   */
  def foreach[R: AsResult](test: SqlQueryRunner => R): Result = {
    val withSqlConnection = new WithPlayTransaction(database)
    val runner = SqlQueryRunner(withSqlConnection)
    Evolutions.cleanupEvolutions(database)
    val result = AsResult(test(runner))
    applyEvolutions()
    result
  }

  val parser =
    (SqlParser.str("author_name") ~ SqlParser.str("book_name")).map {
      case authorName ~ bookName => (authorName, bookName)
    }

  "WithPlayTransaction" should {

    "test 1=1" in { runner: SqlQueryRunner =>
      1 must beTypedEqualTo(1)
    }

    "execute a query fetching an author" in { runner: SqlQueryRunner =>
      val query = SqlQuery(
        implicit c =>
          SQL"select author_name from author where id = 1".as(
            SqlParser.str("author_name").single
        )
      )
      val result = runner(query)
      result must beTypedEqualTo("Martin Odersky").await
    }

    "execute a query fetching an author and his book (with a join)" in {
      runner: SqlQueryRunner =>
        val query = SqlQuery(
          implicit c =>
            SQL"select author_name, book_name from author a join book b on a.id = b.author_id where a.id = 1"
              .as(parser.single)
        )
        val result = runner(query)
        result must beTypedEqualTo(("Martin Odersky", "Programming in Scala")).await
    }

    "execute a query which insert an author and his book" in {
      runner: SqlQueryRunner =>
        val insertQuery = for {
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into author values (2, 'Sam Haliday')".executeInsert()
          )
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into book values (2, 'Functional programming in Scala for mortals', 2018, 'Packt', 2)"
                .executeInsert()
          )
        } yield ()

        runner(insertQuery) must beTypedEqualTo(()).await

        val getQuery = SqlQuery(
          implicit c =>
            SQL"select author_name, book_name from author a join book b on a.id = b.author_id where a.id = 2"
              .as(parser.single)
        )

        runner(getQuery) must beTypedEqualTo(
          ("Sam Haliday", "Functional programming in Scala for mortals")
        ).await
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

    "not insert an author or the book when the book table is written with a typo" in {
      runner: SqlQueryRunner =>
        val query = for {
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into author values (3, 'Sam Haliday')".executeInsert()
          )
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into boookk values (2, 'Functional programming in Scala for mortals', 2018, 'Packt', 3)"
                .executeInsert()
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
                    SQL"select id from author where id = 3"
                      .as(SqlParser.int(1).singleOpt)
                )
              )
          }
        queryResult.flatten must throwA[org.h2.jdbc.JdbcSQLException](
          "Table \"BOOOKK\" not found"
        ).await
        result must beNone.await
    }

  }

}
