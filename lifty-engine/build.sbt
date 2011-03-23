version := "0.7"

resolvers += "Scala Tools Releases" at "http://scala-tools.org/repo-releases/"

libraryDependencies += "org.scalatest" % "scalatest" % "1.3" % "test"

libraryDependencies += "net.liftweb" % "lift-json_2.8.1" % "2.3-M1" % "compile->default"

libraryDependencies += "com.googlecode.scalaz" % "scalaz-core_2.8.0" % "5.0" % "compile->default"

libraryDependencies += "jline" % "jline" % "0.9.94" % "compile->default"

libraryDependencies += "org.fusesource.scalate" % "scalate-core" % "1.4.1" % "compile->default"

name := "lifty-engine"

organization := "org.lifty"