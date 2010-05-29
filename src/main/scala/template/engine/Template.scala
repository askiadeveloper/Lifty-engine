package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{BoxUtil}

trait Template {
		
	def name: String
	def arguments: List[Argument]
	def files: List[TemplateFile]
  def fixedValues = List[ArgumentResult]()

	def process(operation: String, argumentsList: List[String]): CommandResult = {
		operation match {
			case "create" if supportsOperation("create") => this.asInstanceOf[Create].create(argumentsList)
			case "delete" if supportsOperation("delete") => this.asInstanceOf[Delete].delete(argumentsList)
			case _ => CommandResult("bollocks")
		}
	}
	
	//#Protected 
	
	//  Takes a list of argument formatted strings (i.e. name=value) and returns a boxed list of
	//  ArgumentResults. The actual parsing of the values is done by each subclass of Argument
	protected def parseArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {

    // List[net.liftweb.common.Box[List[template.engine.ArgumentResult]]]
    val listOfBoxes = arguments.map( _.parseList(argumentsList))
    val anyFailures = BoxUtil.containsAnyFailures(listOfBoxes)
    if (anyFailures) {
      val failureMsgs = 
        listOfBoxes.filter( _.isInstanceOf[Failure]).map(_.asInstanceOf[Failure].msg).mkString("\n")
      Failure(failureMsgs)
    } else {
      val boxedArguments = 
        listOfBoxes.filter(_.isInstanceOf[Full[List[ArgumentResult]]]).flatMap(_.open_!)
      Full(boxedArguments)
    }
  }
  
	//#Private 
	
	//  Checks if a template supports the operation
	private def supportsOperation(operation: String): Boolean = operation match {
		case "create" => this.isInstanceOf[Create]
		case "delete" => this.isInstanceOf[Delete] 
		case _ => false
	}
}