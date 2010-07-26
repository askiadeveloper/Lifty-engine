package template.engine

import sbt._
import processor.{Processor, ProcessorResult}
import sbt.processor.BasicProcessor
import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.File
import template.util.{FileHelper,TemplateHelper}
import template.engine.commands._

trait TemplateProcessor {
  
  /**
  * Declaring a type logger. The logger has to have the same
  * method signature for logging as the SBT logger
  * 
  */
  type Logger = {

    def error (message: => java.lang.String): Unit
    def info (message: => java.lang.String): Unit
    def warn (message: => java.lang.String): Unit
    def success (message: => java.lang.String): Unit

  }
  
  /**
  * The logger to use to print errors, infos warnings etc.
  * 
  * @return     The logger
  */
  def log: Logger
  
  /**
  * The list of templates that the processor can create
  * 
  * @return The templates the processor should be able
  *         to create.
  */
  def templates: List[Template]
  
  /**
  * The commands that the processor knows. You can override this
  * if you want to add your own commands but remember to call 
  * ::: super.commands at the end of the method so you don't 
  * loose the create, help etc. commands.
  * 
  * @param  commands  The commands that the processor knows
  * @return           The commands that the processor knows
  */
  def commands: List[Command] = List(CreateCommand(this), /*DeleteCommand,*/ TemplatesCommand(this), HelpCommand(this))
  
  /**
  * This is the entry point. It will process the input and invoke
  * the right command with the right arguments.
  * 
  * @param  args  The string to parse
  * @return       A box with the result of processing the input
  */
  def processInput(args: String): Box[String] = {

    val argsArr = args.split(" ")
    val keyword = argsArr(0)
    val arguments = argsArr.toList - keyword
  
    resolveCommand(keyword) match {
      case Full(command) => command.run(arguments) match {
        case Full(cmdrslt) => Full(cmdrslt.message)
        case Failure(msg,_,_) => Failure(msg)
        case Empty => Failure("empty")
      }
      case Failure(msg,_,_) => Failure(msg)
      case Empty => Failure(resolveCommand("help").open_!.run(Nil).open_!.message)
    }
  }
  
  /**
  * Finds the command with the specific keyword. 
  * 
  * @param  keyword The keyword ie. name of the command
  * @return Full(theCommand) if a command with the keyword exists, otherwise Failure
  */
  def resolveCommand(keyword: String): Box[Command] = {
    commands.filter( command => command.keyword == keyword) match {
      case command :: rest => Full(command)
      case Nil => 
        log.error("Command %s does not exist".format(keyword))
        Empty
    }
  }

  /**
  * This methods find a template with the given name. It searches the list return by 
  * the templates method on a processor.
  * 
  * @param  name  The name of the template to search for
  * @return       A Full[Template] containing a template if it exists, otherwise Failure
  */
  def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
      case template :: rest => Full(template) 
      case Nil => Empty
  }
}