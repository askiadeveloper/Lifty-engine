version := "0.7"

resolvers += "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"

libraryDependencies += "org.scalatest" % "scalatest" % "1.3" % "test"

libraryDependencies += "net.liftweb" % "lift-json_2.8.1" % "2.3-M1" % "compile->default"

libraryDependencies += "com.googlecode.scalaz" % "scalaz-core_2.8.0" % "5.0"

name := "lifty-engine"

organization := "org.lifty"