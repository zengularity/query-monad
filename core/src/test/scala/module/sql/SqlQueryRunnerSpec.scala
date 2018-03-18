package com.zengularity.querymonad.test.core.module.sql

import acolyte.jdbc.{
  AcolyteDSL,
  ExecutedParameter,
  QueryExecution,
  QueryResult => AcolyteQueryResult
}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import com.zengularity.querymonad.core.module.sql.{
  SqlQuery,
  SqlQueryRunner,
  SqlQueryT,
  WithSqlConnection
}
import com.zengularity.querymonad.test.core.module.sql.models.{
  Material,
  Professor
}
import com.zengularity.querymonad.test.core.module.sql.utils.SqlConnectionFactory

class SqlQueryRunnerSpec(implicit ee: ExecutionEnv) extends Specification {

  "SqlQueryRunner" should {
    // execute lift Queries
    "return integer value lift in Query using pure" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query  = SqlQuery.pure(1)

      runner(query) aka "material" must beTypedEqualTo(1).await
    }

    "return optional value lift in Query using liftF" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query  = SqlQueryT.liftF(Seq(1))

      runner(query) aka "material" must beTypedEqualTo(Seq(1)).await
    }

    // execute single query
    "retrieve professor with id 1" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(Professor.resultSet)
      val runner = SqlQueryRunner(withSqlConnection)
      val result = runner(Professor.fetchProfessor(1)).map(_.get)

      result aka "professor" must beTypedEqualTo(
        Professor(1, "John Doe", 35, 1)).await
    }

    "retrieve material with id 1" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(Material.resultSet)
      val runner = SqlQueryRunner(withSqlConnection)
      val result = runner(Material.fetchMaterial(1)).map(_.get)

      result aka "material" must beTypedEqualTo(
        Material(1, "Computer Science", 20, "Beginner")).await
    }

    "not retrieve professor with id 2" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        _         <- SqlQuery.ask
        professor <- Professor.fetchProfessor(2)
      } yield professor

      runner(query) aka "material" must beNone.await
    }

    // execute composed queries into a single transaction
    "retrieve professor with id 1 and his material" in {
      val handler = AcolyteDSL.handleQuery {
        case QueryExecution("SELECT * FROM professors where id = ?",
                            ExecutedParameter(1) :: Nil) =>
          Professor.resultSet
        case QueryExecution("SELECT * FROM materials where id = ?",
                            ExecutedParameter(1) :: Nil) =>
          Material.resultSet
        case _ =>
          AcolyteQueryResult.Nil
      }
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(handler)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        professor <- Professor.fetchProfessor(1).map(_.get)
        material  <- Material.fetchMaterial(professor.material).map(_.get)
      } yield (professor, material)

      runner(query) aka "professor and material" must beTypedEqualTo(
        Tuple2(Professor(1, "John Doe", 35, 1),
               Material(1, "Computer Science", 20, "Beginner"))).await
    }

    "not retrieve professor with id 1 and no material" in {
      import cats.instances.option._
      val handler = AcolyteDSL.handleQuery {
        case QueryExecution("SELECT * FROM professors where id = {id}", _) =>
          Professor.resultSet
        case _ =>
          AcolyteQueryResult.Nil
      }
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(handler)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        professor <- SqlQueryT.fromQuery(Professor.fetchProfessor(1))
        material <- SqlQueryT.fromQuery(
          Material.fetchMaterial(professor.material))
      } yield (professor, material)

      runner(query) aka "professor and material" must beNone.await
    }

    // execute async queries
    "retrieve int value fetch in an async context" in {
      import scala.concurrent.Future
      import anorm.{SQL, SqlParser}
      import acolyte.jdbc.RowLists
      import acolyte.jdbc.Implicits._
      val queryResult: AcolyteQueryResult =
        (RowLists.rowList1(classOf[Int] -> "res").append(5))
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(queryResult)
      val runner = SqlQueryRunner(withSqlConnection)
      val query =
        SqlQueryT { implicit connection =>
          Future {
            Thread.sleep(900) // to simulate a slow-down
            SQL("SELECT 5 as res")
              .as(SqlParser.int("res").single)
          }
        }

      runner(query) aka "result" must beTypedEqualTo(5).await
    }
  }

}
