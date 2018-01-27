import Dependencies._

name := """query-monad-code"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.4"

// Common values
val commonSettings = Seq(
  organization := "com.zengularity",
  scalaVersion := "2.12.4",
  crossPaths := false,
  scalacOptions ++= Seq(
    "-deprecation", // Emit warning and location for usages of deprecated APIs.
    "-encoding",
    "utf-8", // Specify character encoding used by source files.
    "-explaintypes", // Explain type errors in more detail.
    "-feature", // Emit warning and location for usages of features that should be imported explicitly.
    "-unchecked", // Enable additional warnings where generated code depends on assumptions.
    "-Xcheckinit", // Wrap field accessors to throw an exception on uninitialized access.
    "-Xfatal-warnings", // Fail the compilation if there are any warnings.
    "-Xlint",
    "-Yno-adapted-args", // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
    "-Ypartial-unification", // Enable partial unification in type constructor inference
    "-Ywarn-dead-code", // Warn when dead code is identified.
    "-Ywarn-extra-implicit", // Warn when more than one implicit parameter section is defined.
    "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
    "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
    "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
    "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
    "-Ywarn-numeric-widen", // Warn when numerics are widened.
    "-Ywarn-unused", // Warn if unused.
    "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
  ),
  scalacOptions in (Compile, console) ~= (_.filterNot(
    Set(
      "-Ywarn-unused:imports",
      "-Xfatal-warnings"
    ))),
  scalacOptions in (Test, compile) ~= (_.filterNot(
    Set(
      "-Ywarn-unused:imports",
      "-Xfatal-warnings"
    ))),
  resolvers ++= Seq[Resolver](
    Resolver.sonatypeRepo("releases")
  )
)

scalafmtOnCompile := true
scalafmtConfig := file("project/scalafmt.conf")

wartremoverErrors ++= Warts.unsafe

// Projects definitions

lazy val core = (project in file("core"))
  .settings(commonSettings)
  .settings(
    name := "query-core",
    libraryDependencies ++= Seq(
      Dependencies.cats
    )
  )

lazy val sampleAppExample = (project in file("examples/sample-app"))
  .enablePlugins(PlayScala)
  .settings(commonSettings)
  .settings(
    name := "sample-app-example",
    libraryDependencies ++= Seq(
      jdbc,
      Dependencies.anorm,
      Dependencies.h2,
      Dependencies.scalaTestPlusPlay
    )
  )
  .dependsOn(core)

lazy val root: Project = project
  .in(file("."))
  .aggregate(core, sampleAppExample)
