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
			val destinationPath = replaceVariablesInPath(templateFile.destination)
			val buffer = new StringWriter()
			val context = new DefaultRenderContext(new PrintWriter(buffer))
			addArgumentsToContext(context)
			context.attributes("thePackage") = Helper.packageOfPath(destinationPath)
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
 	CommandResult("[success] Ran %s with arguments:\n - %s"
		.format(template.name, argumentResults.mkString("\n - "),bufferedFiles.mkString("\n")))
	}
	
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
 
	// looks through the path string for any variables (i.e ${someVal}) and replaces
	// it with the acctual value passed to the operation
	private def replaceVariablesInPath(path: String): String = {
		var newPath = path
		"""\$\{(.*)\}""".r.findAllIn(path).toList match {
			case list if !list.isEmpty => {
				list.map(_.toString).foreach{ variable => 
					val argName = variable.replace("${","").replace("}","") //TODO: make prettier?
					newPath = newPath.replace(variable,findValueForArgument(argName))
				}
				newPath
			}
			case _ => path
		}
	}
 
	private def findValueForArgument(name: String): String = {
		try {
			argumentResults.filter( _.name == name ).first.value
		} catch {
			case e: Exception => println(e);""
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