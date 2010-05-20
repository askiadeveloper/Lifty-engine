package template.engine

trait Template {
	
	this: Operation => 
	
	def name: String
	def arguments: List[Argument]

	def supportsOperation(operation: String): Boolean = operation match {
		case "create" => this.isInstanceOf[Create]
		case "delete" => this.isInstanceOf[Delete] 
		case _ => false
	}
	
	def supportsOperation(operation: Operation): Boolean = operation match {
		case deleteOperation: Delete => this.isInstanceOf[Delete]
		case createOperation: Create => this.isInstanceOf[Create]
	}
	
}