package template.engine

import sbt._
import sbt.processor._

trait TemplateProcessor extends Processor {
	
	def templates: List[Template]

	def apply(project: Project, args: String) = { 
		processInput(args)
		new ProcessorResult() 
	}
	
	def processInput(args: String): Unit = {

		val argsArr = args.split(" ")
		val operationName = argsArr(0) 

		// I should be able to make this way better. But for now this will do
		operationName match {
			case "create" | "delete" => {
				val templateName = argsArr(1)
				findTemplate(templateName) match {
					case Some(template) => { 
						val templateArguments = try { argsArr(2)} catch {
							case _ => ""
						}
						template.process(operationName, templateArguments)
					}
					case None => println("Sorry, no template named: %s".format(templateName))
				}
			}
			case "templates" => println("should list templates")
			case "help" => println("should list operations (create delete help)")
		}
	}
	
	private def findTemplate(name: String): Option[Template] = templates.filter( _.name == name) match {
			case template :: rest => Some(template) 
			case Nil => None
	}	
	
}