package com.zengularity.querymonad.examples.todoapp.controller

import java.nio.charset.Charset
import java.util.{Base64, UUID}
import scala.concurrent.{ExecutionContext, Future}

import cats.instances.either._
import play.api.mvc._
import play.api.libs.json.Json
import com.zengularity.querymonad.examples.todoapp.controller.model.AddUserPayload
import com.zengularity.querymonad.examples.todoapp.model.{Credential, User}
import com.zengularity.querymonad.examples.todoapp.store.{
  CredentialStore,
  UserStore
}
import com.zengularity.querymonad.module.sql.{SqlQueryE, SqlQueryRunner}

class UserController(
    runner: SqlQueryRunner,
    store: UserStore,
    credentialStore: CredentialStore,
    cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends AbstractController(cc)
    with Authentication {

  def createUser: Action[AddUserPayload] =
    Action(parse.json[AddUserPayload]).async { implicit request =>
      val payload = request.body
      val query = for {
        _ <- SqlQueryE.fromQuery[String, Unit](
          store.getByLogin(payload.login).map {
            case Some(_) => Left("User already exists")
            case None    => Right(())
          }
        )

        user = AddUserPayload.toModel(payload)(UUID.randomUUID())
        credential = AddUserPayload.toCredential(payload)

        _ <- SqlQueryE.liftQuery[String, Unit](
          credentialStore.saveCredential(credential)
        )
        _ <- SqlQueryE.liftQuery[String, Unit](store.createUser(user))
      } yield ()

      runner(query).map {
        case Right(_)          => NoContent
        case Left(description) => BadRequest(description)
      }
    }

  def getUser(userId: UUID): Action[AnyContent] = ConnectedAction.async {
    request =>
      if (request.userInfo.id == userId)
        runner(store.getUser(userId)).map {
          case Some(user) => Ok(Json.toJson(user))
          case None       => NotFound("The user doesn't exist")
        } else
        Future.successful(NotFound("Cannot operate this action"))
  }

  def deleteUser(userId: UUID): Action[AnyContent] = ConnectedAction.async {
    request =>
      val userInfo = request.userInfo
      if (userInfo.id == userId) {
        val query = for {
          _ <- credentialStore.deleteCredentials(userInfo.login)
          _ <- store.deleteUser(userId)
        } yield ()
        runner(query).map(_ => NoContent.withNewSession)
      } else
        Future.successful(BadRequest("Cannot operate this action"))
  }

  def login: Action[AnyContent] = Action.async { implicit request =>
    val authHeaderOpt = request.headers
      .get("Authorization")
      .map(_.substring("Basic".length()).trim())

    val query = for {
      credential <- SqlQueryE.liftF[String, Credential](
        authHeaderOpt
          .map { encoded =>
            val decoded = Base64.getDecoder().decode(encoded)
            val authStr = new String(decoded, Charset.forName("UTF-8"))
            authStr.split(':').toList
          }
          .collect {
            case login :: password :: _ => Credential(login, password)
          }
          .toRight("Missing credentials")
      )

      exists <- SqlQueryE.liftQuery[String, Boolean](
        credentialStore.check(credential)
      )

      user <- {
        if (exists)
          SqlQueryE.fromQuery[String, User](
            store
              .getByLogin(credential.login)
              .map(_.toRight("The user doesn't exist"))
          )
        else
          SqlQueryE.liftF[String, User](Left("Wrong credentials"))
      }
    } yield user

    runner(query).map {
      case Right(user) =>
        NoContent.withSession("id" -> user.id.toString, "login" -> user.login)
      case Left(description) => BadRequest(description).withNewSession
    }
  }

  def logout: Action[AnyContent] = ConnectedAction {
    NoContent.withNewSession
  }
}
