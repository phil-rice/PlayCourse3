name := "PlayCourse3"
 
version := "1.0" 
      
lazy val `playcourse3` = (project in file(".")).enablePlugins(PlayScala)

resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
      
resolvers += "Akka Snapshot Repository" at "http://repo.akka.io/snapshots/"
      
scalaVersion := "2.12.2"

libraryDependencies ++= Seq( jdbc , ehcache , ws , specs2 % Test , guice )


val versions = new {
  val scalatest = "3.0.1"
  val scalamock = "3.6.0"
}

libraryDependencies += "org.scalatest" %% "scalatest" % versions.scalatest % "test"
libraryDependencies += "org.scalamock" %% "scalamock-scalatest-support" % versions.scalamock % Test


unmanagedResourceDirectories in Test <+=  baseDirectory ( _ /"target/web/public/test" )

