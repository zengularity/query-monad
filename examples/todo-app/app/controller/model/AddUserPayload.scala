package com.zengularity.querymonad.examples.todoapp.controller.model

import java.util.UUID

import play.api.libs.json.{Json, Reads}

import com.zengularity.querymonad.examples.todoapp.model.{Credential, User}

case class AddUserPayload(
    login: String,
    fullName: String,
    password: String
)

object AddUserPayload {

  def toModel(payload: AddUserPayload)(id: UUID): User = User(
    id,
    payload.login,
    payload.fullName
  )

  def toCredential(payload: AddUserPayload): Credential = Credential.build(
    payload.login,
    payload.password
  )

  implicit val format: Reads[AddUserPayload] = Json.reads
}
