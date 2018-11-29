import java.io.File

import sbt.Keys._
import sbt._

object Publish {

  def skipSettings: Seq[Def.Setting[_]] =
    Seq(
      publish / skip := true
    )

  def settings: Seq[Def.Setting[_]] =
    Seq(
      publishTo := {
        import Resolver.mavenStylePatterns

        sys.env
          .get("REPO_PATH")
          .map(path => Resolver.file("repo", new File(path)))
      },
      licenses := Seq(
        "MIT License" -> url(
          "https://github.com/zengularity/query-monad/blob/master/LICENSE"
        )
      ),
      pomIncludeRepository := { _ =>
        false
      },
      autoAPIMappings := true,
      homepage := Some(url("https://github.com/zengularity/query-monad")), // TODO
      apiURL := Some(url("https://github.com/zengularity/query-monad")), // TODO
      scmInfo := Some(
        ScmInfo(url("https://github.com/zengularity/query-monad"),
                "git@github.com:zengularity/query-monad.git")
      )
    )
}
