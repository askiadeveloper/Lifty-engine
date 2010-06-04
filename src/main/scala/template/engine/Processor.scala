package template.engine

import sbt._
import sbt.processor.BasicProcessor
import net.liftweb.common.{Box, Empty, Failure, Full}

case class CommandResult(message: String)
trait Command {
  def keyword: String
  def run(arguments: List[String]): CommandResult
}

trait TemplateProcessor {
  
  def templates: List[Template]
  def commands: List[Command] = List(CreateCommand, DeleteCommand, TemplatesCommand, HelpCommand)
  def configuration: Configuration
  
  def processInput(args: String): Unit = {

    val argsArr = args.split(" ")

    val keyword = argsArr(0)
    val arguments = argsArr.toList - keyword
	
    val result = commands.filter( command => command.keyword == keyword) match {
			case commands if commands.size > 0 => commands.first.run(arguments)
			case Nil => CommandResult("[error] Command is not supported")
		}
		println(result.message)
  }
  
  //# Protected 
  protected def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
      case template :: rest => Full(template) 
      case Nil => Failure("[error] No template with the name %s".format(name))
  }
  
  //#commands
  
  // TODO: Both Create and DeleteCommand are almost identical - refactor slightly
  object CreateCommand extends Command {
    def keyword = "create"
    
    def run(arguments: List[String]): CommandResult = {
      val templateName = arguments(0)
      findTemplate(templateName) match {
        case Full(template) => template.process("create",arguments-arguments(0));
        case Failure(msg,_,_) => CommandResult(msg)
      }
    }
  }

  object DeleteCommand extends Command {
    def keyword = "delete"
    def run(arguments: List[String]): CommandResult = {
      val templateName = arguments(0)
      findTemplate(templateName) match {
        case Full(template) => template.process("delete",arguments-arguments(0));
        case Failure(msg,_,_) => CommandResult(msg)
      }
    }
  }

  object TemplatesCommand extends Command {
    def keyword = "templates"
    def run(arguments: List[String]): CommandResult = CommandResult("[todo] This should list all templates")
  }

  object HelpCommand extends Command {
    def keyword = "help"
    def run(arguments: List[String]): CommandResult = CommandResult("[todo] This should list all commands")
  }
  
}

trait SBTTemplateProcessor extends BasicProcessor with TemplateProcessor {
  
  //TODO: Need to get the real value. Should get the real values from the Project 
  override def configuration = Configuration("src/main/resources")
  
  def apply(project: Project, args: String) = { 
    processInput(args)
  }
}

trait StandAloneTemplateProcessor extends TemplateProcessor {
  
  //TODO: Need to get the real value somehow
  override def configuration = Configuration("src/main/resources")
  
  def main(args: Array[String]): Unit = {
     processInput( args.mkString(" ") )
  }
}
