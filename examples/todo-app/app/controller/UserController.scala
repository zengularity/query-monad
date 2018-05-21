package com.zengularity.querymonad.examples.todoapp.controller

import scala.concurrent.ExecutionContext

import cats.instances.either._
import play.api.mvc._
import play.api.libs.json.Json

import com.zengularity.querymonad.examples.todoapp.store.UserStore
import com.zengularity.querymonad.module.sql.{SqlQueryRunner, SqlQueryT}
import com.zengularity.querymonad.examples.todoapp.model.User

class UserController(
    runner: SqlQueryRunner,
    store: UserStore,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  type ErrorOrResult[A] = Either[String, A]

  def createUser: Action[User] =
    Action(parse.json[User]).async { implicit request =>
      val payload = request.body
      val query = for {
        _ <- SqlQueryT.fromQuery[ErrorOrResult, Unit](
          store.getUser(payload.id).map {
            case Some(_) => Left("User already exists")
            case None    => Right(())
          }
        )
        _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](
          store.createUser(request.body)
        )
      } yield ()

      runner(query).map {
        case Right(_)          => NoContent
        case Left(description) => BadRequest(description)
      }
    }

  def getUser(userId: Int): Action[AnyContent] = Action.async {
    runner(store.getUser(userId)).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound("The user doesn't exist")
    }
  }

  def deleteUser(userId: Int): Action[AnyContent] = Action.async {
    val query = for {
      _ <- SqlQueryT.fromQuery[ErrorOrResult, User](
        store.getUser(userId).map(_.toRight("User doesn't exist"))
      )
      _ <- SqlQueryT.liftQuery[ErrorOrResult, Unit](store.deleteUser(userId))
    } yield ()

    runner(query).map {
      case Right(_)          => NoContent
      case Left(description) => BadRequest(description)
    }
  }

}
