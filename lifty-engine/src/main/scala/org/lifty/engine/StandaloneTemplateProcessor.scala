package org.lifty.engine

import sbt._
import processor.{Processor, ProcessorResult}
import sbt.processor.BasicProcessor
import net.liftweb.common.{Box, Empty, Failure, Full}

/**
* This is the class you want to extend if you're creating an stand alone app 
* 
*/
trait StandAloneTemplateProcessor extends TemplateProcessor {
    
  def log = TemplateEngineLogger  
  
  def main(args: Array[String]): Unit = {
    GlobalConfiguration.rootResources = "src/main/resources" 

    processInput( args.mkString(" ") ) match {
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