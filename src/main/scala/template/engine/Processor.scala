package template.engine

import sbt._
import sbt.processor._
import net.liftweb.common.{Box, Empty, Failure, Full}

case class CommandResult(message: String)
trait Command {
  def keyword: String
  def run(arguments: List[String]): CommandResult
}

trait TemplateProcessor extends Processor {
  
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
  
  def templates: List[Template]

  def apply(project: Project, args: String) = { 
    processInput(args)
    new ProcessorResult() 
  }
  
  def commands = List(CreateCommand, DeleteCommand, TemplatesCommand, HelpCommand)
  
  def processInput(args: String): Unit = {

    val argsArr = args.split(" ")

    val keyword = argsArr(0)
    val arguments = argsArr.toList - keyword

    val commandsList = commands.filter( command => command.keyword == keyword)
    val result = if (commandsList.size > 0) commandsList.first.run(arguments) else CommandResult("[error] Not supported")
    println(result) 
  }
  
  private def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
      case template :: rest => Full(template) 
      case Nil => Failure("[error] No template with the name %s".format(name))
  } 
  
}