package wiring

import scala.concurrent.ExecutionContext

import anorm._

import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.mvc.Results._
import play.api.routing.Router
import play.api.routing.sird._

import database.{Query, QueryRunner}

class AppComponents(context: Context)
    extends BuiltInComponentsFromContext(context)
    with DBComponents
    with HikariCPComponents
    with NoHttpFiltersComponents {

  val db = dbApi.database("default")

  def queryRunner(implicit ec: ExecutionContext) = QueryRunner(db, ec)

  val router: Router = Router.from {

    // Essentially copied verbatim from the SIRD example
    case GET(p"/hello/$to") =>
      Action.async {
        val query = Query.pure(to)
        queryRunner.run(query).map { to =>
          Ok(s"Hello $to")
        }
      }

    case GET(p"/sqrt/${double(num)}") => Action.async {
      /*
       Use Action.async to return a Future result (sqrt can be intense :P)
       Note the use of double(num) to bind only numbers (built-in :)
       */

      val query = Query(implicit c =>
        SQL"select sqrt($num) as result".as(SqlParser.int("result").single))

      queryRunner.run(query).map(r => Ok(r.toString))
    }
  }
}

class AppLoader extends ApplicationLoader {
  def load(context: Context) = new AppComponents(context).application
}
