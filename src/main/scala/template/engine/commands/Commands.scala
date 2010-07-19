package template.engine.commands

import net.liftweb.common._
import template.engine._

trait Command {
  val processor: TemplateProcessor
  def keyword: String
  def description: String
  def run(arguments: List[String]): Box[CommandResult]
}

case class CommandResult(message: String)


case class CreateCommand(processor: TemplateProcessor) extends Command {
  
  def keyword = "create"
  
  def description = "Processes the specified template. Usage: create <templateName> <arguments>"
  
  def run(arguments: List[String]): Box[CommandResult] = {
    arguments match {
      case Nil => 
        processor.log.error("You have to specify which template to create")
        Failure(processor.resolveCommand("templates").open_!.run(Nil).open_!.message)
      case head :: rest => {
        val templateName = head
        processor.findTemplate(templateName) match {
          case Full(template: Template) =>  template.process("create",rest) match {
            case f @ Full(_) => f
            case f @ Failure(_,_,_) => 
              processor.log.error("Couldn't create the template")
              f
            case Empty => Empty
          }
          case _ => 
            processor.log.error("Can't find a template by name %s".format(templateName))
            Failure(processor.resolveCommand("templates").open_!.run(Nil).open_!.message)
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

case class TemplatesCommand(processor: TemplateProcessor) extends Command {
  def keyword = "templates"
  
  def description = "Lists all of the templates"
  
  def run(arguments: List[String]): Box[CommandResult] = {
    val longestTemplateName = processor.templates.map(_.name.length).reduceLeft(_ max _)
    val msg = processor.templates.map{ template => 
      val spaces = (for (i <- 0 to longestTemplateName-template.name.length) yield " ").mkString("")
      val arguments = template.arguments.map(_.name).mkString(",")
      "%s%s   %s".format(template.name, spaces, arguments)
    }.mkString("\n")
    Full(CommandResult("The processor declares the following templates\n\n" + msg))
  }
}

case class HelpCommand(processor: TemplateProcessor) extends Command {
  def keyword = "help"
  
  def description = "Lists all of the commands"
  
  def run(arguments: List[String]): Box[CommandResult] = {
    val longestCommandName = processor.commands.map( _.keyword.length).reduceLeft(_ max _)
    val msg = processor.commands.map{ cmd => 
      val spaces = (for (i <- 0 to longestCommandName-cmd.keyword.length ) yield " ").mkString("")
      "%s%s   %s".format(cmd.keyword, spaces, cmd.description)
    }.mkString("\n") 
    Full(CommandResult("The processor declares the following commands\n\n" + msg))
  }
    
}