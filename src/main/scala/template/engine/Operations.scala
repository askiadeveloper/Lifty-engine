package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 
	// TODO: This needs to return a CommandResult
	def create(argumentList: List[String]): CommandResult = {
		this.parseArguments(argumentList) match {
			case Full(list) => 
				CommandResult("[success] would have invoked %s with arguments %s".format(this.name, list.mkString(",")))
			case Failure(msg,_,_) => CommandResult("[error] " + msg)
			case Empty => CommandResult("[error] It's empty")
		}
	}		
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(argumentList: List[String]): CommandResult = CommandResult("Invkoed delete on template %s".format(this.name))

}