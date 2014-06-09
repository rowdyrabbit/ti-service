import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ti-service"
  val appVersion      = "1.0-SNAPSHOT"
  val scalaVersion    = "2.10.3"

  val appDependencies = Seq(
    javaCore,
    javaJdbc,
    javaEbean,
    cache,
    "ws.securesocial" %% "securesocial" % "2.1.3",
    "com.typesafe" %% "play-plugins-redis" % "2.2.0",
    "com.typesafe.play" %% "play-cache" % "2.2.0"

  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns),
    resolvers += Resolver.url("Typesafe repository", new URL("http://repo.typesafe.com/typesafe/releases/"))(Resolver.ivyStylePatterns),
    resolvers += "pk11 repo" at "http://pk11-scratch.googlecode.com/svn/trunk"

  )

}