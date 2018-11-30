addSbtPlugin("com.geirsson"    % "sbt-scalafmt"    % "1.5.1")
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.3.7")

// The Play plugin, used for the play-sql module
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.6.20")

// Plugins for documentation publishing
addSbtPlugin("com.typesafe.sbt" % "sbt-ghpages" % "0.6.2")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.3.2")
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.4.2")
