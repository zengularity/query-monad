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

  protected type ActionBlock[A] = ConnectedUserRequest[A] => Result
  protected type ActionAsyncBlock[A] = ConnectedUserRequest[A] => Future[Result]

  object ConnectedAction {

    def apply[A](block: => Result) = builder(block)

    def apply[A](block: ActionBlock[AnyContent]) = builder(block)

    def async[A](block: => Future[Result]) = builder.async(block)

    def async[A](block: ActionAsyncBlock[AnyContent]) = builder.async(block)

    def async[A](parser: BodyParser[A])(block: ActionAsyncBlock[A]) =
      builder.async(parser)(block)

    private def builder: ActionBuilder[ConnectedUserRequest, AnyContent] =
      builder[AnyContent](parse.default)

    private def builder[B](
        bodyParser: BodyParser[B]
    ): ActionBuilder[ConnectedUserRequest, B] =
      new ActionBuilder[ConnectedUserRequest, B] {
        def parser: BodyParser[B] = bodyParser

        def executionContext: ExecutionContext = ec

        def invokeBlock[A](
            request: Request[A],
            block: ConnectedUserRequest[A] => Future[Result]
        ): Future[Result] = {
          request.session
            .get("id")
            .flatMap(str => Try(UUID.fromString(str)).toOption)
            .zip(request.session.get("login"))
            .headOption
            .map {
              case (id, login) =>
                ConnectedUserRequest(ConnectedUserInfo(id, login), request)
            }
            .fold(Future.successful(Unauthorized("Missing credentials")))(block)
        }
      }
  }

}
