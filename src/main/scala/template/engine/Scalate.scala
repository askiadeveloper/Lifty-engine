package template.engine

import org.fusesource.scalate.{TemplateEngine,DefaultRenderContext}
import template.util.Helper
import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter, InputStream, FileOutputStream}
import java.net.{URL, URISyntaxException}
import scala.util.matching.Regex

case class Scalate(template: Template with Create, argumentResults: List[ArgumentResult]) {
	
	def run: CommandResult = { 
	 
		val engine = new TemplateEngine
		
		// Setting the classpath of the compiler used by scalate! 
		if (GlobalConfiguration.scalaLibraryPath != "") {
			engine.classpath = (GlobalConfiguration.scalaLibraryPath :: 
													GlobalConfiguration.scalaCompilerPath ::
													GlobalConfiguration.scalatePath :: Nil).mkString(":")
		}
		
		val bufferedFiles = template.files.map{ templateFile =>
			
			val file: File = createTempTemplateFile(templateFile.file)
			val sclateTemplate = engine.load(file.getAbsolutePath)
			val destinationPath = Helper.replaceVariablesInPath(templateFile.destination,argumentResults)
			println(destinationPath) //@DEBUG
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
			} finally {
				// clean up in case the temp filse was generated
				val tempTemplateFile = new File("temptemplatefile.ssp")
				if (tempTemplateFile.exists) tempTemplateFile.delete 
			}
		}
		val stroke = "-----------------%s------------------------------".format(template.name.map(_=>'-').mkString(""))
		val header = "%s\nRunning %s with the following arguments:\n%s".format(stroke,template.name,stroke)
		val arguments = "\n%s\n".format(argumentResults.map(arg => arg.argument.name+" = "+arg.value).mkString("\n"))
		val files = "%s\nResulted in the creation of the following files:\n%s\n%s\n%s"
			.format(stroke,stroke,template.files.map( path => Helper.replaceVariablesInPath(path.destination,argumentResults)).mkString(""),stroke)
		
 		CommandResult("%s%s%s".format(header,arguments,files))
	}
	
	//# private
	
 	// If the app is runnning af a .jar the template file is read and 
	// it writes the content to a temp. file. This is necessary as 
	// Scalate can't find files inside jars.
	private def createTempTemplateFile(path: String): File = {		
		if (new File(path).exists) { // we're not running as a jar.
			new File(path)
		} else {
			try {
				val pahtToResource = "/" + path
				val is = this.getClass().getResourceAsStream(path)
				val in = scala.io.Source.fromInputStream(is)
				val file = new File("temptemplatefile.ssp")
				file.createNewFile
				val out = new BufferedWriter(new FileWriter(file));
				in.getLines.foreach(out.write(_))
				out.close
				file
			} catch {
				case e: Exception => {
					println(e) //@DEBUG
					new File("temptemplatefile.ssp")
				}
			}
		}
	}
	
	// this runs through each of the ArgumentResults and adds them to the template context.
	// repeatable arguments gets added as a list
	private def addArgumentsToContext(context: DefaultRenderContext): Unit = {
		// recursivly run through the list and add any repeatable argument to the
		// context as a list. 
		def addArgs(arg: ArgumentResult, args: List[ArgumentResult]): Unit = {
			val toAdd = args.filter( _.argument.name == arg.argument.name)
			toAdd match {
				case argument :: rest if rest == Nil => {
					// Add any repeatable arugment with value of "" as an empty list
					argument match {
						case empty if empty.argument.isInstanceOf[Repeatable] && empty.value == "" => 
							context.attributes(empty.argument.name) = List[String]()
						case repeatable if repeatable.argument.isInstanceOf[Repeatable] => 
							context.attributes(repeatable.argument.name) = List(repeatable.value)
						case argument => 
							context.attributes(argument.argument.name) = argument.value
					}
				}
				case argument :: rest => {
					val list = argument :: rest
					if (list.forall(_.argument.isInstanceOf[Repeatable])) { //Add repeatable as list
						context.attributes(arg.argument.name) = list.map(_.value)
					} else {
						context.attributes(arg.argument.name) = list.map(_.value).first
					}
				}
				case Nil => // Empty list
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