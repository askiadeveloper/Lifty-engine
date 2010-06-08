package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{BoxUtil}

case class Argument(name: String){
  
	protected def isOptional = false
	protected def hasDefault = false
	protected def default = ""

  //  Point of variation for each of the trait you might mix into argument. Every subclass 
  //  can add extra functions to fit their needs.
  protected var requirements: List[ List[ArgumentResult] => Box[List[ArgumentResult]]] =
   ((argumentResults: List[ArgumentResult]) => {
      argumentResults match {
          case argument :: rest => Full(List(argument))
          // TODO: feels a bit hackish to check the type
          case Nil if hasDefault => Full(List(ArgumentResult(this,default)))
          case Nil if isOptional => Empty
          case Nil => Failure("[error] The argument '%s' is required".format(this.name))
       }
    }) :: Nil

  //  Simply invokes the requirements on the arguments.
  def parseList(list: List[String]): Box[List[ArgumentResult]] = {
    val aLotOfBoxes = requirements.map( _.apply( findArgumentIn(list) ) )
    if( BoxUtil.containsAnyFailures(aLotOfBoxes)){
      val failureMsgs = 
        aLotOfBoxes.filter( _.isInstanceOf[Failure]).map (_.asInstanceOf[Failure].msg).mkString("\n")
      Failure(failureMsgs)
    } else {
      val boxedArguments = 
        aLotOfBoxes.filter(_.isInstanceOf[Full[_]]).flatMap(_.open_!)
      Full(boxedArguments)
    }
  }
  
  //#Protected
  
  //  Find the value(s) for this argument in a list of arguments-formatted
  //  strings i.e. name=value
  protected def findArgumentIn(in: List[String]): List[ArgumentResult] = {
    val regxp = """%s=\w+""".format(this.name).r
    
    in.filter( str => !regxp.findFirstIn(str).isEmpty) 
      .map{ argument: String => 
        val nameAndValue = argument.split("=") 
        val name = nameAndValue(0) 
        val value = nameAndValue(1)
        ArgumentResult(this, value)
      }.toList
  }
}

trait Repeatable extends Argument {
  
  requirements = ((argumentResults: List[ArgumentResult]) => {
    argumentResults match {
      case head :: rest => Full(rest) //ignoring the head because it will get added in function in Argument
			case Nil => Empty
    }
  }) :: requirements
  
}

trait Optional extends Argument {
	
	override def isOptional = true
	
}

trait Default extends Argument{
	
	this: Value => 
	
	override def hasDefault = true
	
}

case class DefaultValue(default: String)

trait Value extends Default {
	

	protected var value :String = ""
	override protected def default = value


}

case class ArgumentResult(argument: Argument, value: String)