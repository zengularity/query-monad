import sbt.Keys._
import sbt.{Def, _}

object Settings {

  def commonLibSettings: Seq[Def.Setting[_]] =
    commonSettings ++ Publish.settings

  def commonExamplesSettings: Seq[Def.Setting[_]] =
    commonSettings ++ Publish.skipSettings

  private def commonSettings =
    Seq(
      organization := "com.zengularity",
      crossPaths := false,
      scalacOptions ++= scalacOptionsVersion(scalaVersion.value),
      scalacOptions in (Compile, console) ~= (_.filterNot(
        Set(
          "-Ywarn-unused:imports",
          "-Xfatal-warnings"
        )
      )),
      scalacOptions in (Test, compile) ~= (_.filterNot(
        Set(
          "-Ywarn-unused:imports",
          "-Xfatal-warnings",
          "-Yrangepos"
        )
      )),
      resolvers ++= Seq[Resolver](
        Resolver.sonatypeRepo("releases")
      )
    )

  private def scalacOptionsVersion(scalaVersion: String) = {
    val defaultOptions = Seq(
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
      "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures.
      "-Ywarn-infer-any", // Warn when a type argument is inferred to be `Any`.
      "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
      "-Ywarn-nullary-unit", // Warn when nullary methods return Unit.
      "-Ywarn-numeric-widen", // Warn when numerics are widened.
      "-Ywarn-unused", // Warn if unused.
      "-Ywarn-value-discard" // Warn when non-Unit expression results are unused.
    )
    val v2_11_Options = Seq(
      "-Xsource:2.12" // See https://github.com/scala/scala/releases/tag/v2.11.11
    )
    val v2_12_Options = Seq(
      "-Ywarn-extra-implicit" // Warn when more than one implicit parameter section is defined.
    )

    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2L, 11L)) => defaultOptions ++ v2_11_Options
      case _               => defaultOptions ++ v2_12_Options
    }
  }
}
