package template.engine

case class Argument(name: String)
case class ArgumentResult(name: String, value: String)

trait Operation{}
trait Create extends Operation{}
trait Delete extends Operation{}