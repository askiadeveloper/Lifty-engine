package template.processors

import template.engine._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Nil
	def files = "src/main/resources/lift_test.ssp" :: Nil
	
}	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
	
	def arguments = {
	  object nameArgument extends Argument("name") with Default {
	    def default = "defaultName"
	  }
  	object fieldArgument extends Argument("field") with Repeatable
  	nameArgument :: fieldArgument :: Nil
	}
	
  def files: List[String] = Nil
}


object LiftGen extends TemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}