package com.zengularity.querymonad.examples.todoapp.controller

import scala.concurrent.ExecutionContext

import cats.instances.either._
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
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  type ErrorOrResult[A] = Either[String, A]

  def addTodo: Action[Todo] =
    Action(parse.json[Todo]).async { implicit request =>
      val todo = request.body
      val query = for {
        _ <- SqlQueryT.fromQuery[ErrorOrResult, User](
          userStore.getUser(todo.authorId).map(_.toRight("User doesn't exist"))
        )
        _ <- SqlQueryT.fromQuery[ErrorOrResult, Unit](
          todoStore.getTodo(todo.id).map {
            case Some(_) => Left("Todo already exists")
            case None    => Right(())
          }
        )
        _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](
          todoStore.addTodo(todo)
        )
      } yield ()

      runner(query).map {
        case Right(_)          => NoContent
        case Left(description) => BadRequest(description)
      }
    }

  def getTodo(todoId: Int): Action[AnyContent] = Action.async {
    runner(todoStore.getTodo(todoId)).map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None       => NotFound
    }
  }

  def listTodo(userId: Int): Action[AnyContent] = Action.async {
    runner(todoStore.listTodo(userId)).map(todos => Ok(Json.toJson(todos)))
  }

  def removeTodo(todoId: Int): Action[AnyContent] = Action.async {
    val query = for {
      - <- SqlQueryT.fromQuery[ErrorOrResult, Todo](
        todoStore
          .getTodo(todoId)
          .map(_.toRight("Todo doesn't exist"))
      )
      _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](
        todoStore.removeTodo(todoId)
      )
    } yield ()

    runner(query).map {
      case Right(_)          => NoContent
      case Left(description) => BadRequest(description)
    }
  }

  def completeTodo(todoId: Int): Action[AnyContent] = Action.async {
    runner(todoStore.completeTodo(todoId)).map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None       => NotFound("The todo doesn't exist")
    }
  }

}
