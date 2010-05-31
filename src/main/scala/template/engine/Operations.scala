package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import org.fusesource.scalate._
import template.util.Helper

import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter}

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 

	def create(argumentList: List[String]): CommandResult = {
		this.parseArguments(argumentList) match {
			case Full(list) => Scalate(this,list).run
			case Failure(msg,_,_) => CommandResult("[error] " + msg)
			case Empty => CommandResult("[error] It's empty")
		}
	}		
	
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(argumentList: List[String]): CommandResult = {
		this.parseArguments(argumentList) match {
			case Full(list) => this.deleteFiles(list)
			case Failure(msg,_,_) => CommandResult("[error] " + msg)
			case Empty => CommandResult("[error] It's empty")
		}
	}
}