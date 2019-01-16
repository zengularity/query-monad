package com.zengularity.querymonad.test.module.playsql.database

import java.sql.SQLException

import anorm._
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import com.zengularity.querymonad.module.future.implicits._
import com.zengularity.querymonad.module.sql.SqlQuery
import com.zengularity.querymonad.test.module.playsql.utils.WithPlayTransactionUseCases._

class WithPlayTransactionSpec(implicit ee: ExecutionEnv) extends Specification {

  implicit val ec = ee.executionContext

  "WithPlayTransaction" should {

    "execute a query fetching an author" in test(useCase1) { runner =>
      val query = SqlQuery { implicit c =>
        SQL"select author_name from author where id = 1"
          .as(SqlParser.str("author_name").single)
      }
      runner(query) must beTypedEqualTo("Martin Odersky").await
    }

    "execute a query fetching an author and his book (with a join)" in test(
      useCase2
    ) { runner =>
      val authorNameWithBookNameParser =
        (SqlParser.str("author_name") ~ SqlParser.str("book_name"))
          .map { case authorName ~ bookName => (authorName, bookName) }
      val query = SqlQuery { implicit c =>
        SQL"select author_name, book_name from author a join book b on a.id = b.author_id where a.id = 1"
          .as(authorNameWithBookNameParser.single)
      }
      runner(query) must beTypedEqualTo(
        ("Martin Odersky", "Programming in Scala")
      ).await
    }

    "execute a query which insert an author and his book" in test1(useCase3) {
      case (runner, step) =>
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
          step.get() must_=== 3
        }
    }

    "fail when inserting into a non existent table" in test1(useCase4) {
      case (runner, step) =>
        val query: SqlQuery[Unit] = SqlQuery(
          implicit c =>
            SQL"insert into fake_table values (2, 100, 2)".executeInsert()
        ).map(_ => ())

        runner(query) must throwA[SQLException](
          "ERROR: relation \"fake_table\" does not exist"
        ).await and { step.get() must_=== 2 }
    }

    "not insert an author or the book when the book table is written with a typo" in test1(
      useCase5
    ) {
      case (runner, step) =>
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
        runner(query) must throwA[Exception](
          "ERROR: relation \"boookk\" does not exist"
        ).await and { step.get() must_=== 3 }
    }

  }

}
