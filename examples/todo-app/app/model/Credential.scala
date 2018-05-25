package com.zengularity.querymonad.examples.todoapp.model

import anorm._
import play.api.libs.json.{Json, OFormat}

case class Credential(
    login: String,
    password: String
)

object Credential {

  def build(login: String, password: String): Credential =
    Credential(login, password) // TODO: encrypt password

  implicit val fmt: OFormat[Credential] = Json.format

  implicit val parser: RowParser[Credential] = Macro.namedParser[Credential]
}
