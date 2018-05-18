package com.zengularity.querymonad.examples.todoapp.store

import anorm._

import com.zengularity.querymonad.examples.todoapp.model.User
import com.zengularity.querymonad.module.sql.SqlQuery

class UserStore() {

  def createUser(user: User): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"INSERT INTO users values (${user.id}, ${user.fullName})"
        .executeInsert()
    }.map(_ => ())

  def getUser(userId: Int): SqlQuery[Option[User]] =
    SqlQuery { implicit c =>
      SQL"SELECT * FROM users WHERE id = $userId".as(User.parser.singleOpt)
    }

  def deleteUser(userId: Int): SqlQuery[Unit] =
    SqlQuery { implicit c =>
      SQL"DELETE FROM users WHERE id = $userId".execute()
    }.map(_ => ())

}
