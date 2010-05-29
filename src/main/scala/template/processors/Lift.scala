package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Nil
	def files = TemplateFile("src/main/resources/snippet.ssp",
	  "src/main/scala/%s/snippet/%s".format(LiftGen.configuration.rootPackage,"mysnippet.scala")) :: Nil

}	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
	
	def arguments = {
	  object nameArgument extends Argument("name") with Default {  
	    def default = "defaultValue"
	  }
  	object fieldArgument extends Argument("field") with Repeatable
  	nameArgument :: fieldArgument :: Nil
	}
	
  def files: List[TemplateFile] = Nil
}


object LiftGen extends SBTTemplateProcessor with StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}