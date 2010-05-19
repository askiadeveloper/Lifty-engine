package template.engine

import sbt._
import sbt.processor._

trait TemplateProcessor extends Processor {
	
	def templates: List[Template]

	def apply(project: Project, args: String) = { new ProcessorResult() }	
	
}