package com.zengularity.querymonad.examples.todoapp.controller

import scala.concurrent.ExecutionContext.Implicits.global

import cats.instances.option._
import play.api.mvc._
import play.api.libs.json.Json

import com.zengularity.querymonad.examples.todoapp.store.{TodoStore, UserStore}
import com.zengularity.querymonad.module.sql.{SqlQueryRunner, SqlQueryT}
import com.zengularity.querymonad.examples.todoapp.model.{Todo, User}

class TodoController(
    runner: SqlQueryRunner,
    todoStore: TodoStore,
    userStore: UserStore,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def addTodo: Action[Todo] =
    Action(parse.json[Todo]).async { implicit request =>
      val todo = request.body
      val query = for {
        user <- SqlQueryT.fromQuery[Option, User](
          userStore.getUser(todo.authorId)
        )
        todo <- SqlQueryT.liftQuery[Option, Unit](
          todoStore.addTodo(todo)
        )
      } yield todo
      runner(query).map(_ => Ok)
    }

  def getTodo(todoId: Int): Action[AnyContent] =
    Action.async {
      runner(todoStore.getTodo(todoId)).map(_ => Ok)
    }

  def removeTodo(todoId: Int): Action[AnyContent] = Action.async {
    runner(todoStore.removeTodo(todoId)).map(_ => Ok)
  }

  def completeTodo(todoId: Int): Action[AnyContent] = Action.async {
    runner(todoStore.completeTodo(todoId)).map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None       => NotFound("The todo doesn't exist")
    }
  }

}
