package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Argument("pack") ::  Nil
	def files = {
		val config = LiftGen.configuration
	  val templatePath = "snippet/snippet.ssp".format(config.rootResources)
	  val snippetPath = "${package}/${name}.scala"
	  TemplateFile(templatePath,snippetPath) :: Nil
	}
}
	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
		
	def arguments = {
		val packageArgument = Argument("pack")
		object nameArgument extends Argument("name") with Default with Value{ value = "defaultValue" }
		object fieldArgument extends Argument("fields") with Repeatable with Optional
  	packageArgument :: nameArgument :: fieldArgument :: Nil
	}
	
  def files = {
		val config = LiftGen.configuration
    val templatePath = "mapper/mapper.ssp".format(config.rootResources)
    val mapperPath = "${package}/${name}.scala"
    TemplateFile(templatePath,mapperPath) :: Nil
  }
}


object LiftGen extends StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}

class LiftGen extends SBTTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}