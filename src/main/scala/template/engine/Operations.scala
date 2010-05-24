package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}


case class ProcessResult(message: String)

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 
	
	def create(argumentsString: String): Unit = {
		this.parseArguments(argumentsString) match {
			case Full(list) => 
				println("[success] would have invoked %s with arguments %s".format(this.name, arguments.mkString(",")))
			case Failure(msg,_,_) => println("[error] " + msg)
			case Empty => println("[error] It's empty")
		}
	}		
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(arguments: String): Unit = println("Invkoed delete on template %s".format(this.name))

}