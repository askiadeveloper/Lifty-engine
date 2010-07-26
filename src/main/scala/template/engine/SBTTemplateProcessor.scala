package template.engine

import sbt._
import processor.{Processor, ProcessorResult}
import sbt.processor.BasicProcessor
import java.io.{File}
import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{FileHelper}

/**
* This is the class you want to extend if you're creating an SBT processor
* 
*/
 
trait SBTTemplateProcessor extends BasicProcessor with TemplateProcessor {
  
  var sbtLogger: Logger = null;

  def log = sbtLogger
  
  def apply(project: Project, args: String) = { 
    sbtLogger = SBTLogger(project.log)
    val scalatePath = { // TODO: Must be a prettier way to do this! 
      val scalateJarName = "scalate-core-1.0-SNAPSHOT.jar"
      val base =  project.info.bootPath.absolutePath
      val f = new File(base)
      FileHelper.findFileInDir(f,scalateJarName) match { 
        case Some(file) => file.getAbsolutePath
        case None => throw new Exception("Can't find scalate: %s in a subfolder of: %s ".format(scalateJarName,base))
      }
    } 
    GlobalConfiguration.rootResources = ""
    GlobalConfiguration.scalatePath = scalatePath
    GlobalConfiguration.scalaCompilerPath = project.info.app.scalaProvider.compilerJar.getPath
    GlobalConfiguration.scalaLibraryPath = project.info.app.scalaProvider.libraryJar.getPath
    
    processInput(args) match {
      case Full(msg) => 
        msg.split("\n").foreach(log.info(_))
        log.success("Successful.")
      case Failure(msg,_,_) => 
        msg.split("\n").foreach(log.info(_))
        log.error("Error running the command")
      case Empty => log.error("Error running the command")
    }
  }
}