name := "ti-service"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  "ws.securesocial" %% "securesocial" % "2.1.3"
)     

play.Project.playJavaSettings
