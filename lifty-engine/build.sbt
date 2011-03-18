version := "0.1"

resolvers += "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"

libraryDependencies += "org.scalatest" % "scalatest" % "1.3" % "test"

libraryDependencies += "net.liftweb" % "lift-json_2.8.1" % "2.3-M1" % "compile->default"

name := "xlifty-engine"

organization := "org.lifty"