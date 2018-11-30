import sbt._

object Dependencies {

  // versions
  val Http4sVersion = "0.18.21"

  // Dependencies
  lazy val acolyte = "org.eu.acolyte" %% "jdbc-scala" % "1.0.51"

  lazy val acolytePlay = "org.eu.acolyte" %% "play-jdbc" % "1.0.51"

  lazy val anorm = "org.playframework.anorm" %% "anorm" % "2.6.2"

  lazy val cats = "org.typelevel" %% "cats-core" % "1.4.0"
  
  lazy val catsEffect = "org.typelevel" %% "cats-effect" % "1.0.0"

  lazy val http4sBlazeServer = "org.http4s" %% "http4s-blaze-server" % Http4sVersion
  lazy val http4sCirce       = "org.http4s" %% "http4s-circe"        % Http4sVersion
  lazy val http4sDsl         = "org.http4s" %% "http4s-dsl"          % Http4sVersion

  lazy val h2 = "com.h2database" % "h2" % "1.4.197"

  lazy val postgres = "org.postgresql" % "postgresql" % "42.2.5"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.0"

  lazy val specs2 = "org.specs2" %% "specs2-core" % "4.3.5"

  lazy val jbcrypt = "org.mindrot" % "jbcrypt" % "0.4"

  lazy val logback = "ch.qos.logback"  % "logback-classic" % "1.2.3"

}
