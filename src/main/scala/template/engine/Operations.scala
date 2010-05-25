package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}


case class ProcessResult(message: String)

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 
	// TODO: This needs to return a CommandResult
	def create(argumentList: List[String]): Unit = {
		this.parseArguments(argumentList) match {
			case Full(list) => 
				println("[success] would have invoked %s with arguments %s".format(this.name, list.mkString(",")))
			case Failure(msg,_,_) => println("[error] " + msg)
			case Empty => println("[error] It's empty")
		}
	}		
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(argumentList: List[String]): Unit = println("Invkoed delete on template %s".format(this.name))

}