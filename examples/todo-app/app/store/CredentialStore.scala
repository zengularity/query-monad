package com.zengularity.querymonad.examples.todoapp.store

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.Credential
import com.zengularity.querymonad.module.sql.SqlQuery
import com.zengularity.querymonad.examples.todoapp.util.Crypt

class CredentialStore() {

  def saveCredential(credential: Credential): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO credentials values (${credential.login}, ${credential.password})"
        .executeInsert(SqlParser.scalar[String].singleOpt)
    }.map(_ => ())

  def check(credential: Credential): SqlQuery[Boolean] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM credentials WHERE login = ${credential.login}"
        .as(Credential.parser.singleOpt)
        .map(_.password)
        .map(Crypt.checkPassword(credential.password))
        .getOrElse(false)
    }

  def deleteCredentials(login: String): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM credentials WHERE login = $login".execute()
    }.map(_ => ())

}
