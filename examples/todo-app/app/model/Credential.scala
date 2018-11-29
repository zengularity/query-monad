package com.zengularity.querymonad.examples.todoapp.model

import anorm._
import play.api.libs.json.{Json, OFormat}
import com.zengularity.querymonad.examples.todoapp.util.Crypt

case class Credential(
    login: String,
    password: String
)

object Credential {

  def build(login: String, password: String): Credential = {
    val hashedPassword = Crypt.hashPassword(password)
    Credential(login, hashedPassword)
  }

  implicit val fmt: OFormat[Credential] = Json.format

  implicit val parser: RowParser[Credential] = Macro.namedParser[Credential]
}
