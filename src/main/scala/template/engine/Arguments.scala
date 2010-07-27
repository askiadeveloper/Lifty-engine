package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{BoxUtil, IOHelper, TemplateHelper}

trait BasicArgument {
  val name: String
  
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

  override def toString: String = {
    val extras = if (isOptional || hasDefault) {
      ({if (isOptional) Some("optional") else None} :: 
      {if (hasDefault) Some("default") else None} :: 
      {if (this.isInstanceOf[Repeatable]) Some("repeatable") else None} :: Nil)
      .filter(!_.isEmpty).map(_.get).mkString("(",",",")")
    } else ""
    "%s%s".format(name,extras)
  }
}

// Different convinience argument classes


/**
* This is a plain argument, it has no default value and it is required.
* Use this is none of the other classes that extend BasicArguments fits 
* your needs.
* 
*/
case class Argument(name: String) extends BasicArgument
   
/**
* This instance will convert it's own value when uses in paths to.
* If the value com.sidewayscoding is given as a value it will get
* converted to com/sidewayscoding in paths.
*/
case class PackageArgument(name: String) extends BasicArgument {
  override def transformationForPathValue(before: String) = TemplateHelper.pathOfPackage(before)
}

// Traits to alter the restrictions on the Argument

trait Repeatable extends BasicArgument {
  
  requirements = ((argumentResults: List[ArgumentResult]) => {
    argumentResults match {
      case head :: rest => Full(rest) //ignoring the head because it will get added in function in Argument
      case Nil => Empty
    }
  }) :: requirements
  
}

trait Optional extends BasicArgument {
  
  override def isOptional = true
  
}

trait Default extends BasicArgument{
  
  this: Value => 
  
  override def hasDefault = true
  
}

trait Value extends Default {
  

  protected var value :Box[String] = Empty
  override def default: Box[String] = value


}

case class ArgumentResult(argument: BasicArgument, value: String) {
  def pathValue: String = argument.transformationForPathValue(value)    
  
  override def equals(obj: Any) = obj match {
    case arg: BasicArgument if arg == argument => true
    case argrslt: ArgumentResult => argrslt.argument == argument && argrslt.value == value 
    case _ => false
  }
}