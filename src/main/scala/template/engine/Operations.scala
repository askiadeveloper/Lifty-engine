package template.engine

case class ProcessResult(message: String)

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 
	
	def create(argumentsString: String): ProcessResult = {
		val arguments = this.parseArguments(argumentsString)
		ProcessResult("Invkoed create on template %s with arguments %s".format(this.name, arguments.mkString(",")))	
	}
		
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(arguments: String): ProcessResult = ProcessResult("Invkoed delete on template %s".format(this.name))

}