package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = Argument("name") :: Argument("pack") ::  Nil
	def files = {
		val config = LiftGen.configuration
	  val templatePath = "%s/snippet/snippet.ssp".format(config.rootResources)
	  val snippetPath = "src/main/scala/$${pack}/${name}.scala"
	  TemplateFile(templatePath,snippetPath) :: Nil
	}
}
	

object MapperTemplate extends DefaultLiftTemplate {
	def name = "mapper"
		
	def arguments = {
		object packageArgument extends Argument("pack") {
			override def transformationForPathValue(before: String) = Helper.pathOfPackage(before)
		}
		object nameArgument extends Argument("name") with Default with Value{ value = "defaultValue" }
		object fieldArgument extends Argument("fields") with Repeatable with Optional
  	packageArgument :: nameArgument :: fieldArgument :: Nil
	}
	
  def files = {
		val config = LiftGen.configuration
    val templatePath = "%s/mapper/mapper.ssp".format(config.rootResources)
    val mapperPath = "src/main/scala/${pack}/${name}.scala"
    TemplateFile(templatePath,mapperPath) :: Nil
  }
}


// object LiftGen extends StandAloneTemplateProcessor {
// 	
// 	def templates = SnippetTemplate :: MapperTemplate :: Nil 
// 	
// }

class LiftGen extends SBTTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}
object LiftGen extends LiftGen {}

object StandAloneLiftGen extends StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: Nil 
	
}