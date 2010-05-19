package template.engine

import sbt._
import sbt.processor._

trait TemplateProcessor extends Processor {
	
	def templates: List[Template]

	def apply(project: Project, args: String) = { 
		processInput(args)
		new ProcessorResult() 
	}
	
	def processInput(args: String) = {
		// This is just to get started - I hope that I'll be able to use Scopt for option parsing.
		val argsArr = args.split(" ")
		val templateName = argsArr(1)
		val operationName = argsArr(0) 
		
		findTemplate(templateName, operationName) match {
			case Some(temp) => {
				println("Awesome, found the template %s".format(temp.name))
			}
			case None => {
				println("Sorry, no template by name: %s which supports operation %s".format(templateName, operationName)) //@DEBUG
			}
		}
	}
	
	private def findTemplate(name: String, operation: String): Option[Template] = {
		templates.filter( _.name == name) match {
			case template :: rest => Some(template)
			case Nil => None
		}
	}	
	
}