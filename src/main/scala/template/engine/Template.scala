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
      case false => parseIndexedArguments(addUnderscores(argumentsList))
    }
  }
  
  /**
  * Takes a list of argument strings and maps them to ArgumentResuts in the order typed
  * 
  * @param  argumentsList The list of argument strings to parse
  * @return               A box containing a list of ArgumentResults.
  */
  def parseIndexedArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    var argumentResults: List[Box[ArgumentResult]] = Nil
    for (i <- 0 to this.arguments.size-1) {
      val arg = arguments(i) 
      val value = argumentsList(i)
      argumentResults ::= (arg match {
        case arg: Default if value == "_" => Full(ArgumentResult(arg,arg.default))
        case arg: Argument if value == "_" => Failure("%s doesn't have a default value".format(arg.name))
        case arg: Argument => Full(ArgumentResult(arg,value))
      })
    }
    BoxUtil.containsAnyFailures(argumentResults) match {
      case true => (BoxUtil.collapseFailures(argumentResults))
      case false => Full(argumentResults.map(_.open_!).reverse) // it's okay, I allready checked!
    }
  }
  
  /**
  * Takes a list of arguemnt strings and maps them to ArgumentResults. The strings can be 
  * in any order but should be formattet like this argumentName=value. 
  * 
  * @param  argumentsList The list of argument strings to parse
  * @return               A box containing a list of ArgumentResults.
  */
  def parseNamedArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    val listOfBoxes = arguments.map( _.parseList(argumentsList))
    BoxUtil.containsAnyFailures(listOfBoxes) match {
      case true => 
        Failure(listOfBoxes.filter( _.isInstanceOf[Failure]).map(_.asInstanceOf[Failure].msg).mkString("\n"))
      case false => 
        Full(listOfBoxes.filter(_.isInstanceOf[Full[_]]).flatMap(_.open_!))
    }
  }
  
  //#Protected
  
  protected def deleteFiles(argumentResults :List[ArgumentResult]): CommandResult = {
    val files = this.files.map( path => Helper.replaceVariablesInPath(path.destination, argumentResults))
    val result = files.map { path => 
      val file = new File(path)
      "Deleted: %s : %s".format(path,file.delete.toString)
    }
    CommandResult(result.mkString("\n"))
  }

  //#Private 
  
  /**
  * Replaces "" with _ and adds _ for each missing argument.
  * 
  * @param  args  List of arguments to convert
  * @return       A list of argument strings
  */
  private def addUnderscores(args: List[String]): List[String] = {
    args.map( str => if(str.matches("")) "_" else str ) ::: (for (i <- 0 to arguments.size - args.size-1) yield { "_" }).toList 
  }
  
  //  Checks if a template supports the operation
  private def supportsOperation(operation: String): Boolean = operation match {
    case "create" => this.isInstanceOf[Create]
    case "delete" => this.isInstanceOf[Delete] 
    case _ => false
  }
}