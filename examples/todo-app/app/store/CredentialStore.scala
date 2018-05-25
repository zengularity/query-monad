package com.zengularity.querymonad.examples.todoapp.store

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.Credential
import com.zengularity.querymonad.module.sql.SqlQuery

class CredentialStore() {

  def saveCredential(credential: Credential): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO credentials values (${credential.login}, ${credential.password})"
        .executeInsert()
    }.map(_ => ())

  def check(credential: Credential): SqlQuery[Boolean] =
    SqlQuery { implicit c =>
      SQL"SELECT count(*) as res FROM credentials WHERE login = ${credential.login} AND password = ${credential.password}"
        .as(SqlParser.int("res").single)
    }.map(x => if (x > 0) true else false)

  def deleteCredentials(login: String): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM credentials WHERE login = $login".execute()
    }.map(_ => ())

}
