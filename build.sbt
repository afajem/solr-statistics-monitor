name := "solr-monitor"

version := "1.0-SNAPSHOT"

resolvers ++= Seq(
  "Sonatype Nexus Releases" at "https://oss.sonatype.org/content/repositories/releases",
  "Restlet Repository" at "http://maven.restlet.org"
)

libraryDependencies ++= Seq(
  javaCore,
  "cglib" % "cglib" % "2.2.2",
  "org.apache.solr" % "solr-core" % "4.5.1",
  "org.eclipse.jetty" % "jetty-server" % "8.1.10.v20130312",
  "org.eclipse.jetty" % "jetty-servlet" % "8.1.10.v20130312",
  "org.webjars" %% "webjars-play" % "2.2.0",
  "org.webjars" % "bootstrap" % "3.0.2",
  "org.webjars" % "jquery" % "1.9.1",
  "org.webjars" % "highcharts" % "3.0.7"
)

play.Project.playJavaSettings

