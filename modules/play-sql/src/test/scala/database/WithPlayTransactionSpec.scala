package com.zengularity.querymonad.test.module.playsql.database

import scala.concurrent.Future

import anorm._
import com.zengularity.querymonad.module.sql.{SqlQuery, SqlQueryRunner}
import play.api.db.{Database, Databases}
import play.api.db.evolutions.{Evolution, Evolutions, SimpleEvolutionsReader}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import org.specs2.specification.{BeforeAfterAll, ForEach}
import org.specs2.execute.{AsResult, Result}
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres

import com.zengularity.querymonad.module.sql.SqlQueryRunner
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction

class WithPlayTransactionSpec(implicit ee: ExecutionEnv)
    extends Specification
    with BeforeAfterAll
    with ForEach[SqlQueryRunner] {

  val DB_NAME = "default"
  val USER_NAME = "sa"
  val PASSWORD = "sa"
  lazy val (postgres: EmbeddedPostgres, url: String) = {
    val pg = new EmbeddedPostgres()
    val url = pg.start("localhost", 2345, DB_NAME, USER_NAME, PASSWORD)
    (pg, url)
  }
  lazy val database = Databases(
    driver = "org.postgresql.Driver",
    url = url,
    name = DB_NAME,
    config = Map(
      "logStatements" -> true,
      "autoCommit"    -> false,
      "username"      -> USER_NAME,
      "password"      -> PASSWORD
    )
  )

  def beforeAll = {
    // db initialization
    database
    // applying evolutions
    applyEvolutions(database)
  }

  def afterAll = {
    cleanupEvolutions(database)
    database.shutdown()
    postgres.stop()
  }

  private def cleanupEvolutions(database: Database): Unit = {
    Evolutions.cleanupEvolutions(database)
  }

  private def applyEvolutions(database: Database): Unit = {
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
          "delete from author where id = 1;"
        ),
        Evolution(
          4,
          "insert into book values (1, 'Programming in Scala', 2010, 'O''Reilly', 1);",
          "delete from book where id = 1;"
        )
      )
    )
  }

  /**
   * Clean database, apply evolutions and instanciate runner for test
   */
  def foreach[R: AsResult](test: SqlQueryRunner => R): Result = {
    // applyEvolutions(database)
    val withSqlConnection = new WithPlayTransaction(database)
    val runner = SqlQueryRunner(withSqlConnection)
    val result = AsResult(test(runner))
    // cleanupEvolutions(database)
    result
  }

  val authorNameWithBookNameParser =
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
              .as(authorNameWithBookNameParser.single)
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
              .as(authorNameWithBookNameParser.single)
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
        Future(runner(query)).flatten must throwA[
          org.postgresql.util.PSQLException
        ](
          "ERROR: relation \"fake_table\" does not exist"
        ).await
    }

    "not insert an author or the book when the book table is written with a typo" in {
      runner: SqlQueryRunner =>
        import scala.util.{Failure, Success}
        val query = for {
          _ <- SqlQuery(
            implicit c =>
              SQL"insert into author values (3, 'Josh Suereth')".executeInsert()
          )
          _ <- SqlQuery( implicit c =>
            SQL"insert into boookk values (3, 'Sbt in action', 2014, 'O''Reilly', 3)"
              .executeInsert()
          )
        } yield ()
        val queryResult = runner(query)
        val parser =
          (SqlParser.int("id") ~ SqlParser.str("author_name")).map {
            case id ~ authorName => (id, authorName)
          }
        val getUserQuery =
          SqlQuery(
            implicit c =>
              SQL"select id, author_name from author where id = 3"
                .as(parser.singleOpt)
          )
        val foundUser = queryResult.transformWith {
          case Success(_) =>
            Future.failed(new RuntimeException("This should never happen"))
          case Failure(_) =>
            runner(getUserQuery)
        }
        queryResult must throwA[org.postgresql.util.PSQLException](
          "ERROR: relation \"boookk\" does not exist"
        ).await
        foundUser must beNone.await
    }

  }

}
