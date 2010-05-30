package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Nil
	def files = {
	  val templatePath = "src/main/resources/snippet.ssp"//TODO: mymodel should be the passed name
	  val snippetPath = "src/main/scala/%s/snippet/${name}".format(LiftGen.configuration.rootPackage,"mysnippet.scala")
	  TemplateFile(templatePath,snippetPath) :: Nil
	}

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
	
  def files = {
    val templatePath = "src/main/resources/mapper.ssp" //TODO: mymodel should be the passed name
    val mapperPath = "src/main/scala/%s/model/${name}".format(LiftGen.configuration.rootPackage)
    TemplateFile(templatePath,mapperPath) :: Nil
  }
}


object LiftGen extends SBTTemplateProcessor with StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}