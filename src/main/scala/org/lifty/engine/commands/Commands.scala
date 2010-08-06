package org.lifty.engine.commands

import net.liftweb.common._
import org.lifty.engine._
import org.lifty.util.BoxUtil._

trait Command {
  val processor: TemplateProcessor
  def keyword: String
  def description: String
  def run(arguments: List[String]): Box[CommandResult]
  
  override def toString: String = {
    val keyword =     "  Keyword:    \t%s".format(this.keyword)
    val description = "  Description:\t%s".format(this.description)
    (keyword :: description :: Nil).mkString("\n")
  }
  
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
        rest match {
          case rest if rest.contains("with") => {
            // if the word before or after the current word is "with" then it's a 
            // template we need to create
            val statement: List[String] = head :: Nil ::: rest
            var templateNames = List[String]()
            for { 
              i <- 0 to statement.size-2 if statement(i) == "with"
            } {
              templateNames :::= (statement(i-1) :: statement(i+1) :: Nil)
            }
            templateNames = templateNames.removeDuplicates
            val templates = templateNames.map{str: String => processor.findTemplate(str)}
            if (templates.forall(!_.isEmpty)) { 
              // all the templates exist, we're good to go.
              val arguments = ((rest -- templateNames) - "with").map(_.toString)
              GroupTemplates(templates.map(_.open_!)).process("create",arguments) match {
                case f @ Full(_) => f
                case f @ Failure(_,_,_) => 
                  processor.log.error("Couldn't create the template")
                  f
                case Empty => Empty
              }
            } else {
              // one or more of the templates didn't exist
              val empties = templates.filter(_.isEmpty)
              processor.log.error("Can't find a template(s) by name %s".format(empties.mkString(", ")))
              Failure(processor.resolveCommand("templates").open_!.run(Nil).open_!.message)
            }
          }
          case rest => {
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
    val templates = processor.templates.map(_.toString).mkString("\n\n")
    Full(CommandResult("The processor declares the following templates\n" + templates))
  }
}

case class HelpCommand(processor: TemplateProcessor) extends Command {
  def keyword = "help"
  
  def description = "Lists all of the commands"
  
  def run(arguments: List[String]): Box[CommandResult] = {
    val commands = processor.commands.map(_.toString).mkString("\n\n")
    Full(CommandResult("The processor declares the following commands\n" + commands))
  }
    
}