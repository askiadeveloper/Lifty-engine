package template.processors

import template.engine._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Nil
	
}

object liftGen extends TemplateProcessor {
	
	def templates = SnippetTemplate :: Nil 
	
}