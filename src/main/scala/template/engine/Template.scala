package template.engine

case class Argument(name: String)
case class ArgumentResult(name: String, value: String)

trait Template {
		
	def name: String
	def arguments: List[Argument]

	def process(operation: String, argumentString: String) = {

		val result: ProcessResult = operation match {
			case "create" if supportsOperation("create") => this.asInstanceOf[Create].create(argumentString)
			case "delete" if supportsOperation("delete") => this.asInstanceOf[Delete].delete(argumentString)
			case _ => ProcessResult("bollocks")
		}
		println(result.toString)

	}
	
	// Private 
	
	protected def parseArguments(argumentsString: String): List[ArgumentResult] = {
		val regxp = """\w+=\w+""".r
		if (arguments.size > 0) {
			argumentsString.split(" ")
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
		} else List[ArgumentResult]()	
	}
	
	private def supportsOperation(operation: String): Boolean = operation match {
		case "create" => this.isInstanceOf[Create]
		case "delete" => this.isInstanceOf[Delete] 
		case _ => false
	}
}