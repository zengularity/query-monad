package com.zengularity.querymonad.examples.todoapp.store

import java.util.UUID

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.User
import com.zengularity.querymonad.module.sql.SqlQuery

class UserStore() {

  def createUser(user: User): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO users values (${user.id}, ${user.login}, ${user.fullName})"
        .executeInsert(SqlParser.scalar[String].singleOpt)
    }.map(_ => ())

  def getUser(userId: UUID): SqlQuery[Option[User]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM users WHERE id = $userId".as(User.parser.singleOpt)
    }

  def getByLogin(login: String): SqlQuery[Option[User]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM users WHERE login = $login".as(User.parser.singleOpt)
    }

  def deleteUser(userId: UUID): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM users WHERE id = $userId".execute()
    }.map(_ => ())

}
