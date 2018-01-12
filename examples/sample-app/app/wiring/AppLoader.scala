package com.zengularity.querymonad.examples.wiring

import anorm._
import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.mvc.Results._
import play.api.routing.Router
import play.api.routing.sird._

import com.zengularity.querymonad.core.database.{Query, QueryRunner}
import com.zengularity.querymonad.examples.database.WithPlayTransaction

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents
    with NoHttpFiltersComponents {

  val db = dbApi.database("default")

  val queryRunner = QueryRunner(new WithPlayTransaction(db))

  val router: Router = Router.from {

    // Essentially copied verbatim from the SIRD example
    case GET(p"/hello/$to") =>
      Action.async {
        val query = Query.pure(to)
        queryRunner(query).map { to =>
          Ok(s"Hello $to")
        }
      }

    case GET(p"/sqrt/${double(num)}") =>
      Action.async {
        val query = Query(implicit c =>
          SQL"select sqrt($num) as result".as(SqlParser.int("result").single))

        queryRunner(query).map(r => Ok(r.toString))
      }

    case GET(p"/ping/$cmd") =>
      Action.async {
        val query =
          for {
            number <- Query.pure(42)
            text <- Query(implicit c =>
              SQL"select $cmd as result".as(SqlParser.str("result").single))
          } yield (text + number)

        queryRunner(query).map(r => Ok(r.toString))
      }

  }
}

class AppLoader extends ApplicationLoader {
  def load(context: Context) = new AppComponents(context).application
}
