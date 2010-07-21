package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{BoxUtil, IOHelper}

case class Argument(name: String){
  
  protected def isOptional = false
  
  protected def hasDefault = false
  
  def default: Box[String] = Empty

  // When an argument is used the the path you might want to do some transformations.
  // The use-case for this is to convery package names to the correct folder structure
  def transformationForPathValue(before: String): String = before

  //  Point of variation for each of the trait you might mix into argument. Every subclass 
  //  can add extra functions to fit their needs.
  protected var requirements: List[ List[ArgumentResult] => Box[List[ArgumentResult]]] =
   ((argumentResults: List[ArgumentResult]) => {
      argumentResults match {
          case argument :: rest => Full(List(argument))
          case Nil if hasDefault => default match {
            case Full(str) => Full(List(ArgumentResult(this,str)))
            case Empty => 
              val requestMsg = "No default value for '%s'. Please enter a value: ".format(this.name)
              Full(List(ArgumentResult(this,IOHelper.requestInput(requestMsg))))
            case Failure(msg,_,_) => Failure(msg)
          }
          
          case Nil if isOptional => Full(List(ArgumentResult(this,"")))
          case Nil => 
            val requestMsg = "A value for '%s' is required: ".format(this.name)
            Full(List(ArgumentResult(this,IOHelper.requestInput(requestMsg))))
       }
    }) :: Nil

  //  Simply invokes the requirements on the arguments.
  def parseList(list: List[String]): Box[List[ArgumentResult]] = {
    val aLotOfBoxes = requirements.map( _.apply( findArgumentIn(list)))
    BoxUtil.containsAnyFailures(aLotOfBoxes) match {
      case true => 
        Failure(aLotOfBoxes.filter( _.isInstanceOf[Failure]).map (_.asInstanceOf[Failure].msg).mkString("\n"))
      case false => 
        Full(aLotOfBoxes.filter(_.isInstanceOf[Full[_]]).flatMap(_.open_!))
    }
  }
    
  override def toString: String = {
    val extras = if (isOptional || hasDefault) {
      ({if (isOptional) Some("optional") else None} :: 
      {if (hasDefault) Some("default") else None} :: 
      {if (this.isInstanceOf[Repeatable]) Some("repeatable") else None} :: Nil)
      .filter(!_.isEmpty).map(_.get).mkString("(",",",")")
    } else ""
    "%s%s".format(name,extras)
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

trait Value extends Default {
  

  protected var value :Box[String] = Empty
  override def default: Box[String] = value


}

case class ArgumentResult(argument: Argument, value: String) {
  def pathValue: String = argument.transformationForPathValue(value)    
  
  override def equals(obj: Any) = obj match {
    case arg: Argument if arg == argument => true
    case argrslt: ArgumentResult => argrslt.argument == argument && argrslt.value == value 
    case _ => false
  }
}