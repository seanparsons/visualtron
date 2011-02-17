import sbt._

class VisualtronProject(info: ProjectInfo) extends DefaultProject(info) with IdeaProject {
  lazy val scalaSwing = "org.scala-lang" % "scala-swing" % "2.8.1"
  lazy val groovy = "org.codehaus.groovy" % "groovy-all" % "1.7.8"
  lazy val apacheIvy = "org.apache.ivy" % "ivy" % "2.2.0"
  lazy val specs = "org.scala-tools.testing" %% "specs" % "1.6.7" % "test"
  lazy val junit = "junit" % "junit" % "4.8.2" % "test"
  lazy val mockito = "org.mockito" % "mockito-all" % "1.8.5" % "test"
}
