package com.zengularity.querymonad.test.module.sql.models

import anorm._
import acolyte.jdbc.Implicits._
import acolyte.jdbc.{QueryResult => AcolyteQueryResult}
import acolyte.jdbc.RowLists.rowList4

import com.zengularity.querymonad.module.sql.SqlQuery

case class Material(id: Int, name: String, numberOfHours: Int, level: String)

object Material {
  val schema = rowList4(
    classOf[Int]    -> "id",
    classOf[String] -> "name",
    classOf[Int]    -> "numberOfHours",
    classOf[String] -> "level"
  )

  val parser = Macro.namedParser[Material]

  val resultSet: AcolyteQueryResult =
    Material.schema :+ (1, "Computer Science", 20, "Beginner")

  def fetchMaterial(id: Int): SqlQuery[Option[Material]] =
    SqlQuery { implicit connection =>
      SQL"SELECT * FROM materials where id = $id"
        .as(Material.parser.singleOpt)
    }
}
