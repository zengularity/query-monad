package com.zengularity.querymonad.examples.todoapp.controller.model

import java.util.UUID

import play.api.libs.json.{Json, Reads}

import com.zengularity.querymonad.examples.todoapp.model.Todo

case class AddTodoPayload(
    todoNumber: Int,
    content: String,
    done: Boolean
)

object AddTodoPayload {

  def toModel(payload: AddTodoPayload)(id: UUID, userId: UUID): Todo = Todo(
    id,
    payload.todoNumber,
    payload.content,
    userId,
    payload.done
  )

  implicit val format: Reads[AddTodoPayload] = Json.reads
}
