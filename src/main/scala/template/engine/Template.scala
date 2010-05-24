package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}

case class Argument(name: String)
case class ArgumentResult(name: String, value: String)

trait Template {
		
	def name: String
	def arguments: List[Argument]

	def process(operation: String, argumentString: String) = {

		operation match {
			case "create" if supportsOperation("create") => this.asInstanceOf[Create].create(argumentString)
			case "delete" if supportsOperation("delete") => this.asInstanceOf[Delete].delete(argumentString)
			case _ => ProcessResult("bollocks")
		}
	}
	
	// Protected 
	
	protected def parseArguments(argumentsString: String): Box[List[ArgumentResult]] = {
		val regxp = """\w+=\w+""".r
		if (arguments.size > 0) {
			val argumentresults = argumentsString.split(" ")
				// filter out any invalid formed arguments 
				.filter( str => !regxp.findFirstIn(str).isEmpty) 
				// map them to ArgumentReulsts
				.map{ argument: String => 
					val nameAndValue = argument.split("=") 
					val name = nameAndValue(0) 
					val value = nameAndValue(1)
					ArgumentResult(name, value)
				}
				// filter out whatever arguments we don't care for
				.filter{ argumentResult: ArgumentResult => 
					!arguments.forall( _.name != argumentResult.name)
				}.toList
				if (argumentresults.size == arguments.size) Full(argumentresults) else {
					Failure("The template requires the following arguments %s but only recieved %s"
									.format(arguments.mkString(","),argumentresults.mkString(",")))
				}
		} else Empty
	}
	
	// Private 
	
	private def supportsOperation(operation: String): Boolean = operation match {
		case "create" => this.isInstanceOf[Create]
		case "delete" => this.isInstanceOf[Delete] 
		case _ => false
	}
}