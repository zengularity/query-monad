package com.zengularity.querymonad.test.core.module.sql

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import acolyte.jdbc.{AcolyteDSL, QueryExecution}
import acolyte.jdbc.{QueryResult => AcolyteQueryResult}
import org.specs2.mutable.Specification

import com.zengularity.querymonad.core.module.sql.{
  SqlQuery,
  SqlQueryO,
  SqlQueryT,
  SqlQueryRunner,
  WithSqlConnection
}
import com.zengularity.querymonad.test.core.module.sql.models.{
  Material,
  Professor
}
import com.zengularity.querymonad.test.core.module.sql.utils.SqlConnectionFactory

object SqlQueryRunnerSpec extends Specification {

  "SqlQueryRunner" should {
    // execute lift Queries
    "return integer value lift in Query using pure" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = SqlQuery.pure(1)
      val resF = runner(query)
      val result = Await.result(resF, 1.minute)
      result aka "material" mustEqual 1
    }

    "return optional value lift in Query using liftF" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = SqlQueryT.liftF(Seq(1))
      val resF = runner(query)
      val result = Await.result(resF, 1.minute)
      result aka "material" mustEqual Seq(1)
    }

    // execute single query
    "retrieve professor with id 1" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(Professor.resultSet)
      val runner = SqlQueryRunner(withSqlConnection)
      val resF = runner(Professor.fetchProfessor(1)).map(_.get)
      val result = Await.result(resF, 1.minute)
      result aka "professor" must be equalTo (Professor(1, "John Doe", 35, 1))
    }

    "retrieve material with id 1" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(Material.resultSet)
      val runner = SqlQueryRunner(withSqlConnection)
      val resF = runner(Material.fetchMaterial(1)).map(_.get)
      val result = Await.result(resF, 1.minute)
      result aka "material" mustEqual Material(1,
                                               "Computer Science",
                                               20,
                                               "Beginner")
    }

    "not retrieve professor with id 2" in {
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(AcolyteQueryResult.Nil)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        _ <- SqlQuery.ask
        professor <- Professor.fetchProfessor(2)
      } yield professor
      val resF = runner(query)
      val result = Await.result(resF, 1.minute)
      result aka "material" mustEqual None
    }

    // execute composed queries into a single transaction
    "retrieve professor with id 1 and his material" in {
      val handler = AcolyteDSL.handleStatement
        .withQueryDetection("^SELECT *")
        .withQueryHandler {
          case QueryExecution("SELECT * FROM professors where id = {id}", _) =>
            Professor.resultSet
          case QueryExecution("SELECT * FROM materials where id = {id}", _) =>
            Material.resultSet
          case _ =>
            AcolyteQueryResult.Nil
        }
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(handler)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        professor <- Professor.fetchProfessor(1).map(_.get)
        material <- Material.fetchMaterial(professor.material).map(_.get)
      } yield (professor, material)
      val resF = runner(query)
      val result = Await.result(resF, 1.minute)
      result aka "professor and material" mustEqual Tuple2(
        Professor(1, "John Doe", 35, 1),
        Material(1, "Computer Science", 20, "Beginner"))
    }

    "not retrieve professor with id 1 and no material" in {
      import cats.instances.option._
      val handler = AcolyteDSL.handleStatement
        .withQueryDetection("^SELECT *")
        .withQueryHandler {
          case QueryExecution("SELECT * FROM professors where id = {id}", _) =>
            Professor.resultSet
          case _ =>
            AcolyteQueryResult.Nil
        }
      val withSqlConnection: WithSqlConnection =
        SqlConnectionFactory.withSqlConnection(handler)
      val runner = SqlQueryRunner(withSqlConnection)
      val query = for {
        professor <- SqlQueryO.fromQuery(Professor.fetchProfessor(1))
        material <- SqlQueryO.fromQuery(
          Material.fetchMaterial(professor.material))
      } yield (professor, material)
      val resF = runner(query)
      val result = Await.result(resF, 1.minute)
      result aka "professor and material" mustEqual None
    }
  }

}
