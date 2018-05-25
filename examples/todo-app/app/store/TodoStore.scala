package com.zengularity.querymonad.examples.todoapp.store

import java.util.UUID

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.Todo
import com.zengularity.querymonad.module.sql.SqlQuery

class TodoStore() {

  def addTodo(todo: Todo): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO todos values (${todo.id}, ${todo.content}, ${todo.authorId}, ${false})"
        .executeInsert()
    }.map(_ => ())

  def getTodo(todoId: UUID): SqlQuery[Option[Todo]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM todos WHERE id = $todoId".as(Todo.parser.singleOpt)
    }

  def getByNumber(todoNumber: Int): SqlQuery[Option[Todo]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM todos WHERE todo_number = $todoNumber".as(
        Todo.parser.singleOpt
      )
    }

  def listTodo(userId: UUID): SqlQuery[List[Todo]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM todos WHERE author_id = $userId".as(Todo.parser.*)
    }

  def completeTodo(todoId: UUID): SqlQuery[Option[Todo]] =
    SqlQuery { implicit c =>
      SQL"UPDATE todos SET done = ${true} WHERE id = $todoId".executeUpdate()
    }.flatMap(_ => getTodo(todoId))

  def removeTodo(todoId: UUID): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM todos WHERE id = $todoId".execute()
    }.map(_ => ())

}
