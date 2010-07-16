package template.engine

import sbt._
import processor.{Processor, ProcessorResult}
import sbt.processor.BasicProcessor
import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.File
import template.util.{FileHelper,TemplateHelper}

case class CommandResult(message: String)
trait Command {
  def keyword: String
  def description: String
  def run(arguments: List[String]): Box[CommandResult]
}

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
  
  def log: Logger
  def templates: List[Template]
  def commands: List[Command] = List(CreateCommand, /*DeleteCommand,*/ TemplatesCommand, HelpCommand)
  
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
      case Empty => Failure(HelpCommand.run(Nil).open_!.message)
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
  protected def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
      case template :: rest => Full(template) 
      case Nil => Empty
  }
  
  //#commands
  
  // TODO: Both Create and DeleteCommand are almost identical - refactor slightly
  object CreateCommand extends Command {
    
    def keyword = "create"
    
    def description = "Processes the specified template. Usage: create <templateName> <arguments>"
    
    def run(arguments: List[String]): Box[CommandResult] = {
      arguments match {
        case Nil => 
          log.error("You have to specify which template to create")
          Failure(TemplatesCommand.run(Nil).open_!.message)
        case head :: rest => {
          val templateName = head
          findTemplate(templateName) match {
            case Full(template) =>  template.process("create",rest) match {
              case f @ Full(_) => f
              case f @ Failure(_,_,_) => 
                log.error("Couldn't create the template")
                f
              case Empty => Empty
            }
            case _ => 
              log.error("Can't find a template by name %s".format(templateName))
              Failure(TemplatesCommand.run(Nil).open_!.message)
          }
        }
      }  
    }
  }

  // object DeleteCommand extends Command {
  //   def keyword = "delete"
  //   def description = "Deletes an existing template if possible. Usage: delete <templateName> <arguments> "
  //   def run(arguments: List[String]): Box[CommandResult] = {
  //     val templateName = arguments(0)
  //     findTemplate(templateName) match {
  //       case Full(template) => template.process("delete",arguments-arguments(0));
  //       case Failure(msg,_,_) => Full(CommandResult(msg))
  //       case Empty => Full(CommandResult("no such template")) // TODO: no sure what to do here
  //     }
  //   }
  // }

  object TemplatesCommand extends Command {
    def keyword = "templates"
    
    def description = "Lists all of the templates"
    
    def run(arguments: List[String]): Box[CommandResult] = {
      val longestTemplateName = templates.map(_.name.length).reduceLeft(_ max _)
      val msg = templates.map{ template => 
        val spaces = (for (i <- 0 to longestTemplateName-template.name.length) yield " ").mkString("")
        val arguments = template.arguments.map(_.name).mkString(",")
        "%s%s   %s".format(template.name, spaces, arguments)
      }.mkString("\n")
      Full(CommandResult("The processor declares the following templates\n\n" + msg))
    }
  }

  object HelpCommand extends Command {
    def keyword = "help"
    
    def description = "Lists all of the commands"
    
    def run(arguments: List[String]): Box[CommandResult] = {
      val longestCommandName = commands.map( _.keyword.length).reduceLeft(_ max _)
      val msg = commands.map{ cmd => 
        val spaces = (for (i <- 0 to longestCommandName-cmd.keyword.length ) yield " ").mkString("")
        "%s%s   %s".format(cmd.keyword, spaces, cmd.description)
      }.mkString("\n") 
      Full(CommandResult("The processor declares the following commands\n\n" + msg))
    }
      
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
  
  var sbtLogger: Logger = null;

  def log = sbtLogger
  
  def apply(project: Project, args: String) = { 
    sbtLogger = SBTLogger(project.log)
    val scalatePath = { // TODO: Must be a prettier way to do this! 
      val scalateJarName = "scalate-core-1.0-SNAPSHOT.jar"
      val base =  project.info.bootPath.absolutePath
      val f = new File(base)
      FileHelper.findFileInDir(f,scalateJarName) match { 
        case Some(file) => file.getAbsolutePath
        case None => throw new Exception("Can't find scalate: %s in a subfolder of: %s ".format(scalateJarName,base))
      }
    } 
    GlobalConfiguration.rootResources = ""
    GlobalConfiguration.scalatePath = scalatePath
    GlobalConfiguration.scalaCompilerPath = project.info.app.scalaProvider.compilerJar.getPath
    GlobalConfiguration.scalaLibraryPath = project.info.app.scalaProvider.libraryJar.getPath
    
    processInput(args) match {
      case Full(msg) => 
        msg.split("\n").foreach(log.info(_))
        log.success("Successful.")
      case Failure(msg,_,_) => 
        msg.split("\n").foreach(log.info(_))
        log.error("Error running the command")
      case Empty => log.error("Error running the command")
    }
  }
}

// This is the class you want to extend if you're creating an stand alone app 
trait StandAloneTemplateProcessor extends TemplateProcessor {
    
  def log = TemplateEngineLogger  
  
  def main(args: Array[String]): Unit = {
    GlobalConfiguration.rootResources = "src/main/resources" 

    processInput( args.mkString(" ") ) match {
      case Full(msg) => 
        msg.split("\n").foreach(log.info(_))
        log.success("Successful.")
      case Failure(msg,_,_) => 
        msg.split("\n").foreach(log.info(_))
        log.error("Error running the command")
      case Empty => log.error("Error running the command")
    }
    
  }
}
