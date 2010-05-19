package template.engine

case class Argument(name: String)
case class ArgumentResult(name: String, value: String)

trait Operation{
	
	def operationName: String
	def unapply(theName :String): Option[Operation] = if (theName == operationName) Some(this) else None 
	
}
trait Create extends Operation{
	
	override def operationName = "create"
	
}
trait Delete extends Operation{
	
 	override def operationName = "delete"
}