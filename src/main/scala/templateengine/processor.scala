package templateengine

import sbt._
import sbt.processor._

trait TemplateProcessor extends Processor {
	
	def apply(label: String, project: Project, onFailure: Option[String], args: String): ProcessorResult
	
}