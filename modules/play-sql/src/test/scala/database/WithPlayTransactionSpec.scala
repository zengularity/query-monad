package com.zengularity.querymonad.test.module.playsql.database

import java.sql.SQLException

import acolyte.jdbc.{
  AcolyteDSL,
  QueryResult => AcolyteQueryResult,
  UpdateExecution
}
import acolyte.jdbc.Implicits._
import acolyte.jdbc.play.AcolyteDatabase
import acolyte.jdbc.RowLists.{rowList1, rowList2}
import anorm._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import play.api.db.Database

import com.zengularity.querymonad.module.sql.{
  SqlQuery,
  SqlQueryRunner,
  WithSqlConnection
}
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction

class WithPlayTransactionSpec(implicit ee: ExecutionEnv) extends Specification {

  val authorNameWithBookNameParser =
    (SqlParser.str("author_name") ~ SqlParser.str("book_name")).map {
      case authorName ~ bookName => (authorName, bookName)
    }

  "WithPlayTransaction" should {

    "execute a query fetching an author" in {
      val queryResult = rowList1(
        classOf[String] -> "author_name"
      ) :+ ("Martin Odersky")
      val database: Database =
        new AcolyteDatabase(AcolyteDSL.handleQuery(_ ⇒ queryResult))
      val withSqlConnection: WithSqlConnection =
        new WithPlayTransaction(database)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = SqlQuery(
        implicit c =>
          SQL"select author_name from author where id = 1".as(
            SqlParser.str("author_name").single
        )
      )
      runner(query) must beTypedEqualTo("Martin Odersky").await
    }

    val authorAndBookSchema = rowList2(
      classOf[String] -> "author_name",
      classOf[String] -> "book_name"
    )

    def authorAndBookQueryResult(authorName: String,
                                 bookName: String): AcolyteQueryResult =
      authorAndBookSchema :+ (authorName, bookName)

    "execute a query fetching an author and his book (with a join)" in {
      val queryResult =
        authorAndBookQueryResult("Martin Odersky", "Programming in Scala")
      val database: Database =
        new AcolyteDatabase(AcolyteDSL.handleQuery(_ ⇒ queryResult))
      val withSqlConnection: WithSqlConnection =
        new WithPlayTransaction(database)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = SqlQuery(
        implicit c =>
          SQL"select author_name, book_name from author a join book b on a.id = b.author_id where a.id = 1"
            .as(authorNameWithBookNameParser.single)
      )
      runner(query) must beTypedEqualTo(
        ("Martin Odersky", "Programming in Scala")
      ).await
    }

    "execute a query which insert an author and his book" in {
      @volatile var step = 0
      val handler = AcolyteDSL.handleStatement
        .withUpdateHandler {
          case UpdateExecution("insert into author values (2, 'Sam Haliday')",
                               Nil) =>
            if (step == 0) step = 1
            1
          case UpdateExecution(
              "insert into book values (2, 'Functional programming in Scala for mortals', 2018, 'Packt', 2)",
              Nil
              ) =>
            if (step == 1) step = 2
            1
          case u => throw new SQLException(s"Unexpected updated: $u")
        }
      val resHandler: acolyte.jdbc.ResourceHandler =
        AcolyteDSL.handleTransaction(whenCommit = { _ =>
          if (step == 2) step = 3
          else throw new SQLException("Unexpected commit")
        })
      val database: Database = new AcolyteDatabase(handler, resHandler)
      val withSqlConnection: WithSqlConnection =
        new WithPlayTransaction(database)
      val runner = SqlQueryRunner(withSqlConnection)

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

      runner(insertQuery) must beTypedEqualTo(()).await and {
        step must_=== 3
      }
    }

    "fail when inserting into a non existent table" in {
      @volatile var step = 0
      val handler = AcolyteDSL.handleStatement
        .withUpdateHandler {
          case UpdateExecution("insert into fake_table values (2, 100, 2)",
                               Nil) =>
            if (step == 0) step = 1
            println(s"==========================>>>>>> 1-step: $step")
            throw new SQLException(
              "ERROR: relation \"fake_table\" does not exist"
            )
          case u => throw new SQLException(s"Unexpected update: $u")
        }
      val resHandler = AcolyteDSL.handleTransaction(
        whenRollback = { _ =>
          if (step == 1) {
            step = 2
            println(s"==========================>>>>>> 2-step: $step")
          } else sys.error("Unexpected rollback")
        },
        whenCommit = { _ =>
          sys.error("Unexpected commit")
        }
      )
      val database: Database = new AcolyteDatabase(handler, resHandler)
      val withSqlConnection: WithSqlConnection =
        new WithPlayTransaction(database)
      val runner = SqlQueryRunner(withSqlConnection)

      val query: SqlQuery[Option[Long]] = SqlQuery(
        implicit c =>
          SQL"insert into fake_table values (2, 100, 2)".executeInsert()
      ) //.map(_ => ())
      runner(query) must throwA[SQLException](
        "ERROR: relation \"fake_table\" does not exist"
      ).await and { step must_=== 2 }
    }

    "not insert an author or the book when the book table is written with a typo" in {
      @volatile var step = 0
      val handler = AcolyteDSL.handleStatement
        .withUpdateHandler {
          case UpdateExecution("insert into author values (3, 'Josh Suereth')",
                               Nil) =>
            if (step == 0) step = 1
            1
          case UpdateExecution(
              "insert into boookk values (3, 'Sbt in action', 2014, 'O''Reilly', 3)",
              Nil
              ) =>
            if (step == 1) step = 2
            throw new SQLException("ERROR: relation \"boookk\" does not exist")
          case u => throw new SQLException(s"Unexpected update: $u")
        }
      val resHandler = AcolyteDSL.handleTransaction(whenRollback = { _ =>
        if (step == 2) step = 3
        else throw new SQLException("Unexpected rollback")
      })
      val database: Database = new AcolyteDatabase(handler, resHandler)
      val withSqlConnection: WithSqlConnection =
        new WithPlayTransaction(database)
      val runner = SqlQueryRunner(withSqlConnection)

      val query = for {
        _ <- SqlQuery(
          implicit c =>
            SQL"insert into author values (3, 'Josh Suereth')".executeInsert()
        )
        _ <- SqlQuery(
          implicit c =>
            SQL"insert into boookk values (3, 'Sbt in action', 2014, 'O''Reilly', 3)"
              .executeInsert()
        )
      } yield ()
      val queryResult = runner(query)
      queryResult must throwA[Exception](
        "ERROR: relation \"boookk\" does not exist"
      ).await and { step must_=== 2 }
    }

  }

}
