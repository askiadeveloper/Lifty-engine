package template.processors

import template.engine._
import template.util._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	def name = "snippet"
	def arguments = {
		object packageArgument extends Argument("pack") {
			override def transformationForPathValue(before: String) = Helper.pathOfPackage(before)
		}
		Argument("name") :: packageArgument ::  Nil
	}
	def files = {
	  val templatePath = "%s/snippet.ssp".format(GlobalConfiguration.rootResources)
	  val snippetPath = "src/main/scala/${pack}/${name}.scala"
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
    val templatePath = "%s/mapper.ssp".format(GlobalConfiguration.rootResources)
    val mapperPath = "src/main/scala/${pack}/${name}.scala"
    TemplateFile(templatePath,mapperPath) :: Nil
  }
}

object CometTemplate extends DefaultLiftTemplate {
	
	def name = "comet"
	
	def arguments = {
		object packageArgument extends Argument("pack") {
			override def transformationForPathValue(before: String) = Helper.pathOfPackage(before)
		}
		Argument("name") :: packageArgument ::  Nil
	}
	
	def files = {
	  val templatePath = "%s/comet.ssp".format(GlobalConfiguration.rootResources)
	  val snippetPath = "src/main/scala/${pack}/${name}.scala"
	  TemplateFile(templatePath,snippetPath) :: Nil
	}
	
}

object LiftProjectTemplate extends DefaultLiftTemplate {
	
	def name = "project"
	
	def arguments = {
		object name extends Argument("name")
		object org extends Argument("organization")
		object projectVersion extends Argument("version") 
		  with Default with Value{ value = "0.1"}
		object scalaVersion extends Argument("scala_version") 
		  with Default with Value{ value = "2.7.7"}
		object sbtVersion extends Argument("sbt_version") 
		  with Default with Value{ value = "0.7.4"}
		object scalaBuildVersion extends Argument("scala_build_version") 
		  with Default with Value{ value = "0.7.4"}
		name :: org :: projectVersion :: scalaVersion :: 
		sbtVersion :: scalaBuildVersion :: Nil
	}
		
	def files = {
		val basePath = "%s/basic-lift-project".format(GlobalConfiguration.rootResources)

		val propertiesFilePath = basePath + "/build_properties.ssp"
		val propertiesFileDest = "${name}/project/build.properties"

		val projectDefinitionPath = basePath + "/ProjectDefinition.ssp"
		val projectDefinitionDest = "${name}/project/build/ProjectDefinition.scala"

		TemplateFile(propertiesFilePath, propertiesFileDest) :: 
		TemplateFile(projectDefinitionPath, projectDefinitionDest) :: 
		Nil
	}
	

	
}

// object LiftGen extends StandAloneTemplateProcessor {
// 	
// 	def templates = SnippetTemplate :: MapperTemplate :: Nil 
// 	
// }

class LiftGen extends SBTTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: CometTemplate :: LiftProjectTemplate :: Nil 
	
}
object LiftGen extends LiftGen {}

object StandAloneLiftGen extends StandAloneTemplateProcessor {
	
	def templates = SnippetTemplate :: MapperTemplate :: CometTemplate :: LiftProjectTemplate :: Nil 
	
}