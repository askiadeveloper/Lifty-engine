import sbt._

class ScalateParentProject(info: ProjectInfo) extends DefaultProject(info) 
{	
	val liftcommon = "net.liftweb" % "lift-common" % "2.0-SNAPSHOT" % "compile->default"
	val scalate = "org.fusesource.scalate" % "scalate-core" % "1.1" 
	
	val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
	val scalatools_snapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
	val scalatools_release = "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/"
}