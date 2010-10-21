package org.lifty.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.{File}
import org.lifty.engine.commands.{CommandResult}
import org.lifty.util.{BoxUtil, TemplateHelper, IOHelper}

/**
* A template file is used to keep a relation between a path to a file
* and where you want to place that file once it has been rendered/copied
* 
*/
case class TemplateFile(file: String, destination: String)

/**
* A template is something you want your processor to be able to creates. 
* Create an object or class that mixins in this trait and hand it to the 
* processor and the framework takes care of the rest.
* 
*/
trait Template {
  
  private var _injections = List[TemplateInjection]()
    
  /**
  * The name of the template. This is used to figure out which template the 
  * user is trying to invoke when writing processorName create <templateName>.
  * 
  * @param  name  The name of the template
  * @return       The name of the template
  */
  def name: String

  /**
  * This is the list of arguments that the template needs. 
  * 
  * @param  arguments The list of arguments
  * @return           The list of arguments
  */
  def arguments: List[BasicArgument]
  
  /**
  * Override this to provide a message for the user once the template has successfully
  * been processed.
  * 
  * @return  The notice to print
  */
  def notice(args: List[ArgumentResult]): Box[String] = Empty
  
  /**
  * Provides a description of the template. This will get used to help the users
  * know what they're creating.
  * 
  * @return A description of the template
  */
  def description: String
  
  /**
  * The is the list of files that you want your template to process once invoked
  * 
  * @param  files A list of TemplateFile
  * @return       The list of TemplateFile
  */
  def files: List[TemplateFile]
  
  /**
  * Override this method if you want your template to depend on other templates.
  * The order of the templates is important. If there are any conflicts with the
  * files declared in the templates the conflicting files in the first named template 
  * will get used
  * 
  * @return   The list of templates the template depends on
  */
  def dependencies = List[Template]() 
    
  /**
  * This method is used as an entry point to the DSL. 
  * 
  * @param  path  path to the file whose content you want to inject
  * @return       An instance of Injectable. This does nothing by itself. 
  */
  def injectContentsOfFile(path: String) = Injectable(path, this)
  
  /**
  * I really can't remember why I have this method. Sorry.
  */
  def fixedValues = List[ArgumentResult]()

  /**
  * This method is invoked before trying to process all the files returned by the
  * files method. Override this method if you have anything you want to do before
  * the files are being processed.
  * 
  * @param  arguments The list of arguments handed to the template by the user
  */
  def preRenderAction(arguments: List[ArgumentResult]): Unit = {}
  
  /**
  * This method is invoked after the template files have been processed. Override
  * this method if you want to do something after the files have been provessed.
  * 
  * @param  arguments well isn't it obvious
  */
  def postRenderAction(arguments: List[ArgumentResult]): Unit = {}

  /**
  * Call this method if you want the template to process it's files. 
  * 
  * @param  operation     The operation to run on the processer ie. create or delete
  * @param  argumentsList The arguments you want to use under the processing of the 
                          template.
  * @return               A CommandResult noting if it went well or not.
  */
  def process(operation: String, argumentsList: List[String]): Box[CommandResult] = {
    operation match {
      case "create" if supportsOperation("create") => this.asInstanceOf[Create].create(argumentsList)
      case "delete" if supportsOperation("delete") => this.asInstanceOf[Delete].delete(argumentsList)
      case _ => Failure("Bollocks!")
    }
  }
    
  /**
  * Gets all of the files declared in this tempalte and each of it's dependencies
  * IMPORTANT: If there are any duplicated templates (i.e. files that have the same
  *            output path) the first in the list will be used. 
  *
  * @return The files of this template and of each dependency (if any)
  */
  def getAllFiles: List[TemplateFile] = {  
    // checks if a list of template files contains a template file
    // with the same destination as templ
    def contains(templatesFiles: List[TemplateFile], 
                          templ: TemplateFile): Boolean = {
      !templatesFiles.forall( _.destination != templ.destination )
    }

    var cleanList = List[TemplateFile]()
    (files ::: (dependencies.flatMap(_.files))).foreach{ item => 
      if (!contains(cleanList,item)) cleanList ::= item
    }
    cleanList.reverse
  }

  /**
  * Gets all of the dependencies i.e. not just the direct dependencies but also the
  * dependencies og the dependencies
  * 
  * @return All dependencies (deep)
  */
  def getAllDependencies: List[Template] = {
    
    def getDependencies(template: Template): List[Template] = 
      template :: template.dependencies.flatMap(getDependencies(_))
    
    dependencies.flatMap(getDependencies(_))
  }
  
  /**
  * @return A list of arguments declared in the template and each of it's dependencies
  */
  def getAllArguments: List[BasicArgument] = {
    
    def contains(listArgs: List[BasicArgument], arg: BasicArgument) = {
      !listArgs.forall( _.name != arg.name)
    }
    
    var cleanList = List[BasicArgument]()
    (arguments ::: (dependencies.flatMap(_.arguments))).foreach{ arg => 
      if (!contains(cleanList, arg)) cleanList ::= arg
    }
    cleanList.reverse
    
  }

  /**
  * @return A list of TemplateInjections that the template wants to inject.
  */
  def injections: List[TemplateInjection] = _injections
  
  /**
  * This adds a TemplateInjection to the list of TemplateInjections
  * 
  * @param  injection     the TemplateInjection to add
  */
  def addInjection(injection: TemplateInjection) = _injections ::= injection
  
  /**
  * Checks if the template declares any dependencies.
  * 
  * @return true if the templates has any dependencies
  */
  def hasDependencies: Boolean = dependencies.size > 0
    
  /**
  * Takes a list of argument formatted strings (i.e. name=value or just value) and returns a boxed list of
  * ArgumentResults. The actual parsing of the values is done by each subclass of Argument
  * 
  * @param  argumentsList List of argumenst to parse
  * @return A box containing a list of ArgumentResults.
  */
  def parseArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    parseIndexedArguments(addQuestionmarks(argumentsList))
  }

  /**
  * Takes a list of argument strings and maps them to ArgumentResuts in the order typed
  * 
  * @param  argumentsList The list of argument strings to parse
  * @return               A box containing a list of ArgumentResults.
  */
  def parseIndexedArguments(argumentsList: List[String]): Box[List[ArgumentResult]] = {
    
    val isRepeatable: String => Boolean = _.contains(",") 
    var argumentResults: List[Box[ArgumentResult]] = Nil
    val arguments = this.getAllArguments
        
    for (i <- 0 to arguments.size-1) {
      val arg = arguments(i) 
      val value = argumentsList(i)
      argumentResults :::= (arg match {
        case arg: Default if value == "_" => arg.default match {
          case Full(str) => 
            Full(ArgumentResult(arg,str)) :: Nil
          case Empty => 
            val requestMsg = "%s: ".format(arg.name)
            Full(ArgumentResult(arg,IOHelper.requestInput(requestMsg))) :: Nil
          case Failure(msg,_,_) => Failure(msg) :: Nil
        }
        case arg: Default if value == "?" => 
          val dflt = arg.default.openOr("")
          val requestMsg = "%s [%s]: ".format(arg.name, dflt)
          IOHelper.requestInput(requestMsg) match {
            case "" => Full(ArgumentResult(arg,dflt)) :: Nil 
            case str => Full(ArgumentResult(arg,str)) :: Nil 
          }
        case arg: Optional if (value == "_" || value == "?" ) => Full(ArgumentResult(arg,"")) :: Nil
        case arg: BasicArgument if (value == "_" || value == "?" ) => 
          val requestMsg = "%s: ".format(arg.name)
          Full(ArgumentResult(arg,IOHelper.requestInput(requestMsg))) :: Nil
        case arg: Repeatable if isRepeatable(value) => value.split(",").map( v => Full(ArgumentResult(arg,v))).toList
        case arg: BasicArgument => Full(ArgumentResult(arg,value)) :: Nil
      })
    }
    BoxUtil.containsAnyFailures(argumentResults) match {
      case true => (BoxUtil.collapseFailures(argumentResults))
      case false => Full(argumentResults.map(_.open_!).reverse) // it's okay, I allready checked!
    }
  }
    
  override def toString: String = {
    val name =        "  Name:          %s".format(this.name)
    val arguments =   "  Arguments:     %s".format(this.arguments.mkString(","))
    val description = "  Description:   %s".format(this.description)
    (name :: arguments :: description :: Nil).mkString("\n")
  }
    
  protected def deleteFiles(argumentResults :List[ArgumentResult]): Box[CommandResult] = {
    val files = this.files.map( path => TemplateHelper.replaceVariablesInPath(path.destination, argumentResults))
    val result = files.map { path => 
      val file = new File(path)
      "Deleted: %s : %s".format(path,file.delete.toString)
    }
    Full(CommandResult(result.mkString("\n")))
  }
  
  /**
  * Replaces "" with ? and adds ? for each missing (non-optional) argument.
  * 
  * @param  args  List of arguments to convert
  * @return       A list of argument strings
  */
  private def addQuestionmarks(args: List[String]): List[String] = {
    args.map( str => if(str.matches("")) "?" else str ) ::: (for (i <- 0 to getAllArguments.size - args.size-1) yield { "?" }).toList 
  }
  
  /**
  * Checks if the template supports the operation.
  * 
  * @param  operation The operation to run on the template ie. create/delete
  * @return           True if it does, otherwise false.
  */
  private def supportsOperation(operation: String): Boolean = operation match {
    case "create" => this.isInstanceOf[Create]
    case "delete" => this.isInstanceOf[Delete] 
    case _ => false
  }
}

/**
* This will create a Template with no arguments or files but a lot of dependencies.
* This is a convenient way to group templates together
* 
*/
case class GroupTemplates(templates: List[Template]) extends Template with Create {
  def name = ""
  def description = ""
  def files = Nil
  def arguments = Nil
  override def dependencies = templates
}