package com.zengularity.querymonad.examples.todoapp.controller

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

import play.api.mvc._

trait Authentication { self: BaseController =>

  implicit def ec: ExecutionContext

  case class ConnectedUserInfo(
      id: UUID,
      login: String
  )

  case class ConnectedUserRequest[A](
      userInfo: ConnectedUserInfo,
      request: Request[A]
  ) extends WrappedRequest[A](request)

  def ConnectedAction = Action andThen ConnectionRefiner

  private def ConnectionRefiner =
    new ActionRefiner[Request, ConnectedUserRequest] {
      def executionContext = ec
      def refine[A](request: Request[A]) =
        Future.successful(
          request.session
            .get("id")
            .flatMap(str => Try(UUID.fromString(str)).toOption)
            .zip(request.session.get("login"))
            .headOption
            .map {
              case (id, login) =>
                ConnectedUserRequest(ConnectedUserInfo(id, login), request)
            }
            .toRight(Unauthorized("Missing credentials"))
        )
    }

}
