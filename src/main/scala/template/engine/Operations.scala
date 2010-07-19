package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import org.fusesource.scalate._
import template.util.TemplateHelper
import template.engine.commands.{CommandResult}

import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter}

trait Operation{}

trait Create extends Operation {
  
  this: Template with Create => 

  def create(argumentList: List[String]): Box[CommandResult] = {
    this.parseArguments(argumentList) match {
      case Full(list) => Scalate(this,list).run
      case Failure(msg,_,_) => Failure(msg)
      case Empty => Failure("It's empty")
    }
  }   
  
}
trait Delete extends Operation {
  
  this: Template with Delete => 
  
  def delete(argumentList: List[String]): Box[CommandResult] = {
    this.parseArguments(argumentList) match {
      case Full(list) => this.deleteFiles(list)
      case fail @ Failure(_,_,_) => fail
      case Empty => Failure("It's empty")
    }
  }
}