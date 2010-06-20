package template.processors

import template.engine._
import template.util._
import java.io.File

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
	
	val basePath = "%s/basic-lift-project".format(GlobalConfiguration.rootResources)
	
	def name = "project"
	
	def arguments = {
		object mainPackage extends Argument("pack") with Default with Value { value = "code" }
		List(mainPackage)
	}
	
	override def postRenderAction(arguments: List[ArgumentResult]): Unit = {
		Helper.createFolderStructure(arguments)(
			"src/main/resources",
			"src/main/scala",
			"src/main/scala/bootstrap",
			"src/main/scala/bootstrap/liftweb",
			"src/main/scala/${pack}",
			"src/main/scala/${pack}/comet",
			"src/main/scala/${pack}/lib",
			"src/main/scala/${pack}/model",
			"src/main/scala/${pack}/snippet",
			"src/main/scala/${pack}/view",
			"src/main/webapp"
		)
		Helper.copy(
			new File("%s/test/LiftConsole.scala".format(basePath)),
			new File(Helper.replaceVariablesInPath("src/test/scala/${pack}", arguments))
		)
		Helper.copy(
			new File("%s/test/RunWebApp.scala".format(basePath)),
			new File(Helper.replaceVariablesInPath("src/test/scala/${pack}", arguments))
		)
		Helper.copy(
			new File("%s/webapp".format(basePath)),
			new File(Helper.replaceVariablesInPath("src/main/webapp", arguments))
		)
		Helper.copy(
			new File("%s/resources".format(basePath)),
			new File(Helper.replaceVariablesInPath("src/main/resources", arguments))
		)
	}
		
	def files = {

		val projectDefinitionPath = basePath + "/ProjectDefinition.ssp"
		val projectDefinitionDest = "project/build/ProjectDefinition.scala"
		
		val boot = basePath + "/boot.ssp"
		val bootDest = "src/main/scala/bootstrap/Boot.scala"
		
		val helloworld = basePath + "/helloworld.ssp"
		val helloworldDest = "src/main/scala/${pack}/snippet/HelloWorld.scala"
		
		// test files
		val apptest = basePath + "/test/AppTest.ssp"
		val apptestDest = "src/test/scala/${pack}/AppTest.scala"
		
		val helloworldtest = basePath + "/test/HelloWorldTest.ssp"
		val helloworldtestDest = "src/test/scala/${pack}/snippet/HelloWorldTest.scala"
		
		List( 
			TemplateFile(projectDefinitionPath, projectDefinitionDest),
			TemplateFile(boot, bootDest),
			TemplateFile(helloworld, helloworldDest),
			TemplateFile(helloworldtest, helloworldtestDest),
			TemplateFile(apptest, apptestDest)
		)
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