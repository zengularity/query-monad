import sbt._

object Dependencies {
  lazy val acolyte = "org.eu.acolyte" %% "jdbc-scala" % "1.0.47" % Test

  lazy val anorm = "org.playframework.anorm" %% "anorm" % "2.6.0"

  lazy val cats = "org.typelevel" %% "cats-core" % "1.0.1"

  lazy val h2 = "com.h2database" % "h2" % "1.4.196"

  lazy val specs2 = "org.specs2" %% "specs2-core" % "4.0.2" % Test
}
