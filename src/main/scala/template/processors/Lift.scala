package template.processors

import template.engine._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Nil
	
}	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
	def arguments = Argument("name") :: Argument("field") :: Nil

}


object liftGen extends TemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}