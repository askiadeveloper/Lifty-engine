package template.engine

import sbt._
import sbt.processor._
import net.liftweb.common.{Box, Empty, Failure, Full}

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
					case Full(template) => { 
						val templateArguments = try { argsArr(2)} catch {
							case _ => ""
						}
						template.process(operationName, templateArguments)
					}
					case Failure(msg,_,_) => println(msg)
				}
			}
			case "templates" => println("should list templates")
			case "help" => println("should list operations (create delete help)")
			case str: String => 
				println("[error] Operation %s is not supported. Run help for at list of operations".format(str))
		}
	}
	
	private def findTemplate(name: String): Box[Template] = templates.filter( _.name == name) match {
			case template :: rest => Full(template) 
			case Nil => Failure("[error] No template with the name %s".format(name))
	}	
	
}