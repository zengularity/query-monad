package com.zengularity.querymonad.test.core.module.sql.models

import anorm._
import acolyte.jdbc.Implicits._
import acolyte.jdbc.{QueryResult => AcolyteQueryResult}
import acolyte.jdbc.RowLists.rowList4

import com.zengularity.querymonad.core.module.sql.SqlQuery

case class Professor(id: Int, name: String, age: Int, material: Int)

object Professor {
  val schema = rowList4(
    classOf[Int] -> "id",
    classOf[String] -> "name",
    classOf[Int] -> "age",
    classOf[Int] -> "material"
  )

  val parser = Macro.namedParser[Professor]

  val resultSet: AcolyteQueryResult =
    Professor.schema :+ (1, "John Doe", 35, 1)

  def fetchProfessor(id: Int): SqlQuery[Option[Professor]] =
    SqlQuery { implicit connection =>
      SQL"SELECT * FROM professors where id = $id"
        .as(Professor.parser.singleOpt)
    }
}
