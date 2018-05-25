package com.zengularity.querymonad.examples.todoapp.wiring

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.evolutions.EvolutionsComponents
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.routing.Router
import router.Routes

import com.zengularity.querymonad.module.sql.{SqlQueryRunner}
import com.zengularity.querymonad.module.playsql.database.WithPlayTransaction
import com.zengularity.querymonad.examples.todoapp.controller.{
  TodoController,
  UserController
}
import com.zengularity.querymonad.examples.todoapp.store.{
  CredentialStore,
  TodoStore,
  UserStore
}

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with EvolutionsComponents
    with HikariCPComponents
    with NoHttpFiltersComponents {

  val db = dbApi.database("default")

  val queryRunner = SqlQueryRunner(new WithPlayTransaction(db))

  // Stores
  val userStore: UserStore = new UserStore()
  val todoStore: TodoStore = new TodoStore()
  val credentialStore: CredentialStore = new CredentialStore()

  val router: Router = new Routes(
    httpErrorHandler,
    new UserController(queryRunner,
                       userStore,
                       credentialStore,
                       controllerComponents),
    new TodoController(queryRunner, todoStore, userStore, controllerComponents)
  )

  applicationEvolutions.start()
}

class AppLoader extends ApplicationLoader {
  def load(context: Context) = new AppComponents(context).application
}
