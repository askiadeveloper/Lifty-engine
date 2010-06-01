package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Nil
	def files = {
		val config = LiftGen.configuration
	  val templatePath = "%s/snippet/snippet.ssp".format(config.rootResources)
	  val snippetPath = "%s/%s/snippet/${name}.scala".format(config.rootSourceFiles, config.mainPackage)
	  TemplateFile(templatePath,snippetPath) :: Nil
	}
}	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
		
	def arguments = {
		object nameArgument extends Argument("name") with Default with Value{ value = "defaultValue" }
		object fieldArgument extends Argument("fields") with Repeatable with Optional
  	nameArgument :: fieldArgument :: Nil
	}
	
  def files = {
		val config = LiftGen.configuration
    val templatePath = "%s/mapper/mapper.ssp".format(config.rootResources)
    val mapperPath = "%s/%s/model/${name}.scala".format(config.rootSourceFiles, config.mainPackage)
    TemplateFile(templatePath,mapperPath) :: Nil
  }
}


object LiftGen extends SBTTemplateProcessor with StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}