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

class SimpleBuildToolTemplateEngine(info: ProjectInfo) extends ProcessorProject(info) {	
	
	val scalatools_release = "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"
	val scalatools_snapshots = "Scala Tools Snapshot" at "http://scala-tools.org/repo-snapshots/"
	
	val liftcommon = "net.liftweb" % "lift-common" % "2.0-M5" % "compile->default"
  val scalate = "com.sidewayscoding" % "scalate-core" % "1.0-SNAPSHOT"
	val scalatest = "org.scalatest" % "scalatest" % "1.0" % "test"
	
	override def unmanagedClasspath = super.unmanagedClasspath +++ 
		(Path.fromFile(buildScalaInstance.compilerJar)) +++ 
		(Path.fromFile(buildScalaInstance.libraryJar.getPath))

	override def compileOptions = super.compileOptions ++
	    Seq("-unchecked","-encoding", "utf8").map(x => CompileOption(x))
	
	// PUBLISHING
	override def managedStyle = ManagedStyle.Ivy
	val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
	Credentials(Path.userHome / "dev" / ".nexus_credentials", log) 
	
}