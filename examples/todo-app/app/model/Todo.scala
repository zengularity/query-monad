package com.zengularity.querymonad.examples.todoapp.model

import play.api.libs.json.{Json, OFormat}
import anorm.{Macro, RowParser}

case class Todo(
    id: Int,
    content: String,
    authorId: Int,
    done: Boolean
)

object Todo {
  implicit val format: OFormat[Todo] = Json.format
  implicit val parser: RowParser[Todo] = Macro.namedParser[Todo]
}
