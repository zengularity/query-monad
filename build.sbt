ThisBuild / organization := "com.zengularity"

ThisBuild / scalaVersion := "2.12.7"
ThisBuild / crossScalaVersions := Seq("2.11.12", "2.12.7")

// Scalafmt
ThisBuild / scalafmtOnCompile := true

// Wartremover
wartremoverErrors ++= Warts.unsafe

//
// Projects definitions
//

val nameRoot = "query-monad"

// Core + Modules

lazy val core = (project in file("core"))
  .settings(
    Settings.commonLibSettings ++ Seq(
      name := s"$nameRoot-core",
      libraryDependencies ++= Seq(
        Dependencies.acolyte % Test,
        Dependencies.anorm   % Test,
        Dependencies.cats,
        Dependencies.specs2 % Test
      )
    )
  )

lazy val playSqlModule = (project in file("modules/play-sql"))
  .settings(Settings.commonLibSettings)
  .settings(
    name := s"$nameRoot-play-sql",
    libraryDependencies ++= Seq(
      jdbc,
      evolutions               % Test,
      logback                  % Test,
      Dependencies.acolyte     % Test,
      Dependencies.acolytePlay % Test,
      Dependencies.anorm       % Test,
      Dependencies.h2          % Test,
      Dependencies.scalaLogging,
      Dependencies.specs2 % Test
    )
  )
  .dependsOn(core % "test->test;compile->compile")

// Examples

lazy val sampleAppExample = (project in file("examples/sample-app"))
  .enablePlugins(PlayScala)
  .settings(
    Settings.commonExamplesSettings ++ Seq(
      name := s"$nameRoot-example-sample-app",
      name := "sample-app-example",
      libraryDependencies ++= Seq(
        Dependencies.anorm,
        Dependencies.h2
      )
    )
  )
  .dependsOn(core, playSqlModule)

lazy val todoAppExample = (project in file("examples/todo-app"))
  .enablePlugins(PlayScala)
  .settings(Settings.commonExamplesSettings)
  .settings(
    name := s"$nameRoot-example-todo-app",
    libraryDependencies ++= Seq(
      evolutions,
      Dependencies.anorm,
      Dependencies.postgres,
      Dependencies.jbcrypt
    ),
    play.sbt.routes.RoutesKeys.routesImport := Seq(
      "java.util.UUID"
    )
  )
  .dependsOn(core, playSqlModule)

// Aggregate all projects

lazy val root: Project = project
  .in(file("."))
  .aggregate(core, playSqlModule, sampleAppExample, todoAppExample)
  .settings(Publish.skipSettings)
