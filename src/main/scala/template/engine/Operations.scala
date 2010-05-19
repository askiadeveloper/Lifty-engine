package template.engine

case class Argument(optionName: String)

trait Operation{}
trait Create extends Operation{}
trait Delete extends Operation{}