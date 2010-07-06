package template.engine

import sbt._
import processor.{Processor, ProcessorResult}
import sbt.processor.BasicProcessor
import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.File
import template.util.Helper

case class CommandResult(message: String)
trait Command {
  def keyword: String
  def description: String
  def run(arguments: List[String]): CommandResult
}

trait TemplateProcessor {
  
  def templates: List[Template]
  def commands: List[Command] = List(CreateCommand, DeleteCommand, TemplatesCommand, HelpCommand)
  
  def processInput(args: String): Unit = {

    val argsArr = args.split(" ")
    val keyword = argsArr(0)
    val arguments = argsArr.toList - keyword
  
    val result = resolveCommand(keyword) match {
      case Full(command) => command.run(arguments).message
      case Failure(msg,_,_) => msg
      case Empty => "[error] Command is not supported"
    }
    println(result)
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
      case Nil => Failure("[error] Command is not supported")
    }
  }

  /**
  * This methods find a template with the given name. It searches the list return by 
  * the templates method on a processor.
  * 
  * @param  name  The name of the template to search for
  * @return       A Full[Template] containing a template if it exists, otherwise Failure
  */
  protected def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
      case template :: rest => Full(template) 
      case Nil => Failure("[error] No template with the name %s".format(name))
  }
  
  //#commands
  
  // TODO: Both Create and DeleteCommand are almost identical - refactor slightly
  object CreateCommand extends Command {
    def keyword = "create"
    def description = "Processes the specified template. Usage: create <templateName> <arguments>"
    def run(arguments: List[String]): CommandResult = {
      val templateName = arguments(0)
      findTemplate(templateName) match {
        case Full(template) => template.process("create",arguments-arguments(0));
        case Failure(msg,_,_) => CommandResult(msg)
        case Empty => CommandResult("no such template") // TODO: no sure what to do here
      }
    }
  }

  object DeleteCommand extends Command {
    def keyword = "delete"
    def description = "Deletes an existing template if possible. Usage: delete <templateName> <arguments> "
    def run(arguments: List[String]): CommandResult = {
      val templateName = arguments(0)
      findTemplate(templateName) match {
        case Full(template) => template.process("delete",arguments-arguments(0));
        case Failure(msg,_,_) => CommandResult(msg)
        case Empty => CommandResult("no such template") // TODO: no sure what to do here
      }
    }
  }

  object TemplatesCommand extends Command {
    def keyword = "templates"
    def description = "Lists all of the templates"
    def run(arguments: List[String]): CommandResult = CommandResult(templates.map( _.name ).mkString("\n"))
  }

  object HelpCommand extends Command {
    def keyword = "help"
    def description = "Lists all of the commands"
    def run(arguments: List[String]): CommandResult = CommandResult(commands.map( _.keyword ).mkString("\n"))
  }
  
}

// Used to store information about the classpath and other stuff that is different
// for the app if it's running as a processor vs. sbt console
object GlobalConfiguration {
  var scalaCompilerPath = ""
  var scalaLibraryPath = ""
  var scalatePath = ""
  var rootResources = ""
  var runningAsJar = 
    new File(this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath).getAbsolutePath.contains(".jar")
}

// This is the class you want to extend if you're creating an SBT processor
trait SBTTemplateProcessor extends BasicProcessor with TemplateProcessor {
  
  def apply(project: Project, args: String) = { 
    val scalatePath = { // TODO: Must be a prettier way to do this! 
      val base =  project.info.bootPath.absolutePath
      val f = new File(base)
      Helper.findFileInDir(f,"scalate-core-1.0-local.jar") match { 
        case Some(file) => file.getAbsolutePath
        case None => throw new Exception("Can't find scalate in a subfolder of: " + base)
      }
      // base + "/scala-2.7.7/sbt-processors/com.sidewayscoding/sbt_template_engine/0.1/scalate-core-1.0-local.jar"
    } 
    println("new scalatePath: " + scalatePath) //@DEBUG
    GlobalConfiguration.rootResources = ""
    GlobalConfiguration.scalatePath = scalatePath
    GlobalConfiguration.scalaCompilerPath = project.info.app.scalaProvider.compilerJar.getPath
    GlobalConfiguration.scalaLibraryPath = project.info.app.scalaProvider.libraryJar.getPath
    processInput(args)
  }
}

// This is the class you want to extend if you're creating an stand alone app 
trait StandAloneTemplateProcessor extends TemplateProcessor {
    
  def main(args: Array[String]): Unit = {
    GlobalConfiguration.rootResources = "src/main/resources" 
    processInput( args.mkString(" ") )
  }
}
