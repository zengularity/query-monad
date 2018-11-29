package com.zengularity.querymonad.examples.todoapp.controller

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

import cats.instances.either._
import play.api.mvc._
import play.api.libs.json.Json
import com.zengularity.querymonad.examples.todoapp.controller.model.AddTodoPayload
import com.zengularity.querymonad.examples.todoapp.model.{Todo, User}
import com.zengularity.querymonad.examples.todoapp.store.{TodoStore, UserStore}
import com.zengularity.querymonad.module.sql.{SqlQueryE, SqlQueryRunner}

class TodoController(
    runner: SqlQueryRunner,
    todoStore: TodoStore,
    userStore: UserStore,
    cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(cc)
    with Authentication {

  private def check(
      login: String
  )(block: => Future[Result])(implicit request: ConnectedUserRequest[_]) = {
    if (request.userInfo.login == login)
      block
    else
      Future.successful(BadRequest("Not authorized action"))
  }

  def addTodo(login: String): Action[AddTodoPayload] =
    ConnectedAction.async(parse.json[AddTodoPayload]) { implicit request =>
      check(login) {
        val payload = request.body
        val todo = AddTodoPayload.toModel(payload)(UUID.randomUUID(),
                                                   request.userInfo.id)
        val query = for {
          _ <- SqlQueryE.fromQuery[String, Unit](
            todoStore.getByNumber(todo.todoNumber).map {
              case Some(_) => Left("Todo already exists")
              case None    => Right(())
            }
          )
          _ <- SqlQueryE.liftQuery[String, Unit](
            todoStore.addTodo(todo)
          )
        } yield ()

        runner(query).map {
          case Right(_)          => NoContent
          case Left(description) => BadRequest(description)
        }
      }
    }

  def getTodo(login: String, todoId: UUID): Action[AnyContent] =
    ConnectedAction.async { implicit request =>
      check(login) {
        runner(todoStore.getTodo(todoId)).map {
          case Some(todo) => Ok(Json.toJson(todo))
          case None       => NotFound
        }
      }
    }

  def listTodo(login: String): Action[AnyContent] =
    ConnectedAction.async { implicit request =>
      check(login) {
        val query = for {
          user <- SqlQueryE.fromQuery[String, User](
            userStore.getByLogin(login).map(_.toRight("User doesn't exist"))
          )
          todo <- SqlQueryE.liftQuery[String, List[Todo]](
            todoStore.listTodo(user.id)
          )
        } yield todo

        runner(query).map {
          case Right(todos)      => Ok(Json.toJson(todos))
          case Left(description) => BadRequest(description)
        }
      }
    }

  def removeTodo(login: String, todoId: UUID): Action[AnyContent] =
    ConnectedAction.async { implicit request =>
      check(login) {
        val query = for {
          - <- SqlQueryE.fromQuery[String, Todo](
            todoStore
              .getTodo(todoId)
              .map(_.toRight("Todo doesn't exist"))
          )
          _ <- SqlQueryE.liftQuery[String, Unit](
            todoStore.removeTodo(todoId)
          )
        } yield ()

        runner(query).map {
          case Right(_)          => NoContent
          case Left(description) => BadRequest(description)
        }
      }
    }

  def completeTodo(login: String, todoId: UUID): Action[AnyContent] =
    ConnectedAction.async { implicit request =>
      check(login) {
        runner(todoStore.completeTodo(todoId)).map {
          case Some(todo) => Ok(Json.toJson(todo))
          case None       => NotFound("The todo doesn't exist")
        }
      }
    }
}
