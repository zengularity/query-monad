import sbt._

object Dependencies {
  lazy val anorm = "org.playframework.anorm" %% "anorm" % "2.6.0"

  lazy val cats = "org.typelevel" %% "cats-core" % "1.0.1"

  lazy val h2 = "com.h2database" % "h2" % "1.4.196"

  lazy val scalaTestPlusPlay = "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.1" % Test
}
