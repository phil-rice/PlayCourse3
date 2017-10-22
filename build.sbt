import sbt.compilerPlugin

name := "PlayCourse3"

val versions = new {
  val scalatest = "3.0.1"
  val scalamock = "3.6.0"
}

scalaVersion := "2.12.2"

val basicSettings = Seq(
  version := "1.0",
  scalaVersion := "2.12.2",
  resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases",
  resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/",
  resolvers += Resolver.sonatypeRepo("snapshots"),
  libraryDependencies += "org.scalatest" %% "scalatest" % versions.scalatest % "test",
  libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % versions.scalamock % Test)

val utilitiesSettings = basicSettings ++ Seq(
  libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.6"
  //  libraryDependencies += "org.typelevel" %% "macro-compat" % "1.1.1",
  //  libraryDependencies += "org.scala-lang" % "scala-compiler" % scalaVersion.value % "provided",
  //  libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7",
  //  libraryDependencies += compilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.patch)
)

val playSettings = basicSettings ++ Seq(
  libraryDependencies ++= Seq(jdbc, ehcache, ws, specs2 % Test, guice),
  unmanagedResourceDirectories in Test <+= baseDirectory(_ / "target/web/public/test")
)

lazy val utilities = (project in file("modules/utilities")).
  settings(utilitiesSettings: _*)

lazy val playcourse3 = (project in file(".")).
  settings(playSettings: _*).
  dependsOn(utilities % "test->test;compile->compile").aggregate(utilities).
  enablePlugins(PlayScala)
