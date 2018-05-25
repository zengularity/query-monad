package com.zengularity.querymonad.examples.todoapp.model

import java.util.UUID

import play.api.libs.json.{Json, OFormat}
import anorm.{Macro, RowParser}
import anorm.Macro.ColumnNaming.SnakeCase

case class Todo(
    id: UUID,
    todoNumber: Int,
    content: String,
    authorId: UUID,
    done: Boolean
)

object Todo {
  implicit val format: OFormat[Todo] = Json.format
  implicit val parser: RowParser[Todo] = Macro.namedParser[Todo](SnakeCase)
}
