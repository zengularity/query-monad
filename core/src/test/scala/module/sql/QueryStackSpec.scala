package com.zengularity.querymonad.test.module.sql

import acolyte.jdbc.{
  AcolyteDSL,
  QueryExecution,
  QueryResult => AcolyteQueryResult
}
import acolyte.jdbc.RowLists.rowList1
import anorm.{SQL, SqlParser}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import com.zengularity.querymonad.module.sql.{
  SqlQuery,
  SqlQueryRunner,
  SqlQueryT
}
import com.zengularity.querymonad.module.future.implicits._
import com.zengularity.querymonad.module.sql.future.WithSqlConnectionF
import com.zengularity.querymonad.test.module.sql.utils.SqlConnectionFactory

class QueryStackSpec(implicit ee: ExecutionEnv) extends Specification {

  "QueryStackSpec" should {
    def error(divisor: Int)(nb: Int): String =
      s"$nb cannot be divided by $divisor"

    val (divideBy2, divideBy3) = {
      def divideBy(divisor: Int)(nb: Int): SqlQuery[Either[String, Int]] =
        SqlQuery { implicit c =>
          SQL(s"select ($nb / $divisor)").as(
            SqlParser.get[Int](1).singleOpt
          )
        }.map(_.toRight(error(divisor)(nb)))

      (divideBy(2) _, divideBy(3) _)
    }

    def resultSet(result: Int): AcolyteQueryResult = {
      val schema = rowList1(classOf[Int])
      schema.append(result).asResult
    }

    "test 1" in {
      import cats.instances.either._
      val handler = AcolyteDSL.handleQuery {
        case QueryExecution("select (6 / 2)", Nil) => resultSet(3)
        case QueryExecution("select (3 / 3)", Nil) => resultSet(1)
        case _                                     => AcolyteQueryResult.Nil
      }
      val withSqlConnection: WithSqlConnectionF =
        SqlConnectionFactory.withSqlConnection(handler)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        three <- SqlQueryT.fromQuery(divideBy2(6))
        one   <- SqlQueryT.fromQuery(divideBy3(three))
      } yield one

      runner(query) aka "one" must beTypedEqualTo(Right(1): Either[String, Int]).await
    }
  }

}
