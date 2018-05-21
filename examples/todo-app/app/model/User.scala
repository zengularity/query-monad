package com.zengularity.querymonad.examples.todoapp.model

import play.api.libs.json.{Json, OFormat}
import anorm.{Macro, RowParser}
import anorm.Macro.ColumnNaming.SnakeCase

case class User(
    id: Int,
    fullName: String
)

object User {
  implicit val format: OFormat[User] = Json.format
  implicit val parser: RowParser[User] = Macro.namedParser[User](SnakeCase)
}
