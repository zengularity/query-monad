package com.zengularity.querymonad.examples.todoapp.controller

import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc._
import play.api.libs.json.Json

import com.zengularity.querymonad.examples.todoapp.store.UserStore
import com.zengularity.querymonad.module.sql.SqlQueryRunner
import com.zengularity.querymonad.examples.todoapp.model.User

class UserController(
    runner: SqlQueryRunner,
    store: UserStore,
    cc: ControllerComponents
) extends AbstractController(cc) {

  def createUser: Action[User] =
    Action(parse.json[User]).async { implicit request =>
      runner(store.createUser(request.body)).map(_ => Ok)
    }

  def getUser(userId: Int): Action[AnyContent] = Action.async {
    runner(store.getUser(userId)).map {
      case Some(user) => Ok(Json.toJson(user))
      case None       => NotFound("The user doesn't exist.")
    }
  }

  def deleteUser(userId: Int): Action[AnyContent] = Action.async {
    runner(store.deleteUser(userId)).map(_ => Ok)
  }

}
