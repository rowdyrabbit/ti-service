import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "ti-service"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore,
    javaJdbc,
    javaEbean,
    cache,
    "ws.securesocial" %% "securesocial" % "2.1.3"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += Resolver.url("sbt-plugin-releases", new URL("http://repo.scala-sbt.org/scalasbt/sbt-plugin-releases/"))(Resolver.ivyStylePatterns)
  )


}