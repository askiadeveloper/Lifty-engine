// Copyright 2010 Mads Hartmann Jensen
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// 
//     http://www.apache.org/licenses/LICENSE-2.0
// 
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import sbt._

import java.util.jar.Attributes
import java.util.jar.Attributes.Name._

class SimpleBuildToolTemplateEngine(info: ProjectInfo) extends ProcessorProject(info) 
{	
	val liftcommon = "net.liftweb" % "lift-common" % "2.0-M5" % "compile->default"
	val scalate = "org.fusesource.scalate" % "scalate-core" % "1.0-local" from "http://github.com/downloads/mads379/Simple-Build-Tool-Template-Engine/scalate-core-1.0-SNAPSHOT.jar"
	// val scalaLibrary = "org.scala-lang" % "scala-library" % "2.7.7" % "compile->default"
	// val scalaCompiler = "org.scala-lang" % "scala-compiler" % "2.7.7" % "compile->default"
	// 
	// override def filterScalaJars = false // THIS MIGHT NOT BE THAT GREAT AN IDEA
	
	override def unmanagedClasspath = super.unmanagedClasspath +++ 
		// ("project" / "project" / "scala-2.7.7" / "lib" ##) ** "*.jar"
		(Path.fromFile(buildScalaInstance.compilerJar)) +++ 
		(Path.fromFile(buildScalaInstance.libraryJar.getPath))

	
	// val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
	// val scalatools_snapshot = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
	val scalatools_release = "Scala Tools Snapshot" at "http://scala-tools.org/repo-releases/"
	
	override def compileOptions = super.compileOptions ++
	    Seq("-unchecked","-encoding", "utf8").map(x => CompileOption(x))
	
	// PUBLISHING
	override def managedStyle = ManagedStyle.Maven
	val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
	Credentials(Path.userHome / "dev" / ".nexus_credentials", log) 
	
	
	// // TASKS
	// 	
	// override def packageOptions =
	//     manifestClassPath.map(cp => ManifestAttributes(
	//       (Attributes.Name.CLASS_PATH, cp),
	//       (IMPLEMENTATION_TITLE, "Lift Generator"),
	//       (IMPLEMENTATION_URL, ""),
	//       (IMPLEMENTATION_VENDOR, "Mads Hartmann | GSoC Project")
	//     )).toList :::
	//     getMainClass(false).map(MainClass(_)).toList
	// 
	// // create a manifest with all the jars
	//   override def manifestClassPath = Some(allArtifacts.getFiles
	//     .filter(_.getName.endsWith(".jar"))
	//     .mkString(" "))
	// 
	// def allArtifacts = ("lib" ##) ** "*.jar" +++ 
	// 									 ("lib_managed" / "scala_2.7.7" / "compile" ##) ** "*.jar" +++
	// 									 ("target" / "scala_2.7.7" / "classes" ##) ** "*.class" +++ 
	// 									 // ("src" / "main" / "resources" ##) ** "*" +++
	// 									 ("src" / "main" / "resources") ** "*" +++
	// 									 ("META-INF") ** "*"
	// 
	// 
	// lazy val package_standalone = 
	// 	zipTask(allArtifacts,"target","liftgen.jar") dependsOn (`package`) describedAs("Zips up the distribution.")
	
}