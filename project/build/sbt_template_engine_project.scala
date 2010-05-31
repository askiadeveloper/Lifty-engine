import sbt._

import java.util.jar.Attributes
import java.util.jar.Attributes.Name._

class ScalateParentProject(info: ProjectInfo) extends DefaultProject(info) 
{	
	val liftcommon = "net.liftweb" % "lift-common" % "2.0-SNAPSHOT" % "compile->default"
	
	val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
	val scalatools_snapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
	val scalatools_release = "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/"
	
	// TASKS
		
	override def packageOptions =
    manifestClassPath.map(cp => ManifestAttributes(
      (Attributes.Name.CLASS_PATH, cp),
      (IMPLEMENTATION_TITLE, "Lift Generator"),
      (IMPLEMENTATION_URL, ""),
      (IMPLEMENTATION_VENDOR, "Mads Hartmann | GSoC Project")
    )).toList :::
    getMainClass(false).map(MainClass(_)).toList
	
	// create a manifest with all the jars
  override def manifestClassPath = Some(allArtifacts.getFiles
    .filter(_.getName.endsWith(".jar"))
    .mkString(" "))
	
	def allArtifacts = ("lib" ##) ** "*.jar" +++ 
										 ("lib_managed" / "scala_2.7.7" / "compile" ##) ** "*.jar" +++
										 ("target" / "scala_2.7.7" / "classes" ##) ** "*.class" +++ 
										 ("src" / "main" / "resources" ##) ** "*" +++
										 ("META-INF") ** "*"
	
	
	lazy val package_standalone = 
		zipTask(allArtifacts,"target","liftgen.jar") dependsOn (`package`) describedAs("Zips up the distribution.")
	
}