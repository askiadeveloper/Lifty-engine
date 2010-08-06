package org.lifty.engine

import java.io.{File}

/**
* Used to store information about the classpath and other stuff that is different
* for the app if it's running as a processor vs. sbt console
* 
*/
object GlobalConfiguration {
  
  var scalaCompilerPath = ""
  
  var scalaLibraryPath = ""
  
  var scalatePath = ""
  
  var rootResources = ""
  
  var runningAsJar = 
    new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath).getAbsolutePath.contains(".jar")
}