package template.engine

import org.fusesource.scalate.{TemplateEngine,DefaultRenderContext}
import template.util.Helper
import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter}
import scala.util.matching.Regex

case class Scalate(template: Template with Create, argumentResults: List[ArgumentResult]) {
	
	def run: CommandResult = { 
	 
		val engine = new TemplateEngine
		val bufferedFiles = template.files.map{ templateFile =>
			
			val sclateTemplate = engine.load(templateFile.file)
			val destinationPath = Helper.replaceVariablesInPath(templateFile.destination,argumentResults)
			val buffer = new StringWriter()
			val context = new DefaultRenderContext(new PrintWriter(buffer))
			addArgumentsToContext(context)
			sclateTemplate.render(context)
		 
			try {
				createFolderStructure(destinationPath)
				val currentPath = new File("").getAbsolutePath // TODO: Not sure this is needed.
				val file = new File(currentPath+"/"+destinationPath)
				file.createNewFile
				val out = new BufferedWriter(new FileWriter(file));
				out.write(buffer.toString);
				out.close();
			} catch {
				case e: Exception => println(e) //@DEBUG
			}
		}
		val stroke = "-----------------%s------------------------------".format(template.name.map(_=>'-').mkString(""))
		val header = "%s\nRunning %s with the following arguments:\n%s".format(stroke,template.name,stroke)
		val arguments = "\n%s\n".format(argumentResults.map(arg => arg.name+" = "+arg.value).mkString("\n"))
		val files = "%s\nResulted in the creation of the following files:\n%s\n%s\n%s"
			.format(stroke,stroke,template.files.map( path => Helper.replaceVariablesInPath(path.destination,argumentResults)).mkString(""),stroke)
		
 		CommandResult("%s%s%s".format(header,arguments,files))
	}
	
	//# private
	
	// this runs through each of the ArgumentResults and adds them to the template context.
	// repeatable arguments gets added as a list
	private def addArgumentsToContext(context: DefaultRenderContext): Unit = {
		// recursivly run through the list and add any repeatable argument to the
		// context as a list. 
		def addArgs(arg: ArgumentResult, args: List[ArgumentResult]): Unit = {
			val toAdd = args.filter( _.name == arg.name)
			toAdd match {
				case list if list.size == 1 => {
					context.attributes(arg.name) = list.map(_.value).first
				}
				case list if list.size > 0 => {
					context.attributes(arg.name) = list.map(_.value)
				}
				case Nil => // don't add anything. this should happen though
			}
			val rest = (args -- toAdd)
			rest match {
				case argument :: rest => addArgs(argument,(argument::rest))
				case Nil => // done.
			}
		}
		val allArgs = argumentResults:::template.fixedValues
		if (allArgs.size > 0) {
			addArgs(allArgs.first,allArgs)
		}
	}
 
	// This methods takes a path pointing to a file and creates any folder that doen't
	// exist on that path 
	private def createFolderStructure(path: String) {
		val currentPath = new File("").getAbsolutePath
		(path.split("/").toList-path.split("/").last).foldLeft(currentPath){ (combinedString, newString) => 
			val folder = combinedString +"/"+ newString
			new File(folder).mkdir
			folder
		}
	}
}