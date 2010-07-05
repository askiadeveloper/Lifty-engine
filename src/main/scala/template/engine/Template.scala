package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import template.util.{BoxUtil, Helper}
import java.io.{File}

case class TemplateFile(file: String, destination: String)

trait Template {
    
  def name: String
  def arguments: List[Argument]
  def files: List[TemplateFile]
  def fixedValues = List[ArgumentResult]()

  def preRenderAction(arguments: List[ArgumentResult]): Unit = {}
  def postRenderAction(arguments: List[ArgumentResult]): Unit = {}

  def process(operation: String, argumentsList: List[String]): CommandResult = {
    operation match {
      case "create" if supportsOperation("create") => this.asInstanceOf[Create].create(argumentsList)
      case "delete" if supportsOperation("delete") => this.asInstanceOf[Delete].delete(argumentsList)
      case _ => CommandResult("bollocks")
    }
  }
  
  //#Protected 
  
  /**
  * Takes a list of argument formatted strings (i.e. name=value or just value) and returns a boxed list of
  * ArgumentResults. The actual parsing of the values is done by each subclass of Argument
  * 
  * @param  argumentsList List of argumenst to parse
  * @return A box containing a list of ArgumentResults.
  */
  def parseArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    val regxp = """\w+=\w+""".r
    argumentsList.forall(!regxp.findFirstIn(_).isEmpty) match {
      case true => parseNamedArguments(argumentsList)
      case false => parseIndexedArguments(argumentsList)
    }
  }
  
  def parseIndexedArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    var argumentResults: List[ArgumentResult] = Nil
    println(arguments.size)
    println(argumentsList.size)
    for (i <- 0 to this.arguments.size-1) {
      val arg = this.arguments(i)
      val value = if(i < argumentsList.size) {
        argumentsList(i)
      } else {
        arg.default
      }
      argumentResults ::= ArgumentResult(arg, value)
    }
    Full(argumentResults.reverse)
  }
  
  def parseNamedArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    val listOfBoxes = arguments.map( _.parseList(argumentsList))
    BoxUtil.containsAnyFailures(listOfBoxes) match {
      case true => 
        Failure(listOfBoxes.filter( _.isInstanceOf[Failure]).map(_.asInstanceOf[Failure].msg).mkString("\n"))
      case false => 
        Full(listOfBoxes.filter(_.isInstanceOf[Full[_]]).flatMap(_.open_!))
    }
  }
  
  protected def deleteFiles(argumentResults :List[ArgumentResult]): CommandResult = {
    val files = this.files.map( path => Helper.replaceVariablesInPath(path.destination, argumentResults))
    val result = files.map { path => 
      val file = new File(path)
      "Deleted: %s : %s".format(path,file.delete.toString)
    }
    CommandResult(result.mkString("\n"))
  }

  //#Private 
  
  //  Checks if a template supports the operation
  private def supportsOperation(operation: String): Boolean = operation match {
    case "create" => this.isInstanceOf[Create]
    case "delete" => this.isInstanceOf[Delete] 
    case _ => false
  }
}