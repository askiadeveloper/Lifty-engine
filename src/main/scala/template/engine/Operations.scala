package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import org.fusesource.scalate._
import template.util.Helper

import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter}

trait Operation{}

trait Create extends Operation {
	
	this: Template with Create => 

	def create(argumentList: List[String]): CommandResult = {
		this.parseArguments(argumentList) match {
			case Full(list) => runScalate(list)
			case Failure(msg,_,_) => CommandResult("[error] " + msg)
			case Empty => CommandResult("[error] It's empty")
		}
	}		
	
	//#private
	
	private def runScalate(arguments: List[ArgumentResult]): CommandResult = {
	  
	  val engine = new TemplateEngine
	  val bufferedFiles = this.files.map{ templateFile =>
	    val template = engine.load(templateFile.file)
	    val buffer = new StringWriter()
      val context = new DefaultRenderContext(new PrintWriter(buffer))
      (arguments ::: fixedValues).foreach{ argument => 
        context.attributes(argument.name) = argument.value
      }
      context.attributes("thePackage") = Helper.packageOfPath(templateFile.destination)
      template.render(context)
      
      try {
        // create any folder that doesn't exist
        val currentPath = new File("").getAbsolutePath
        (templateFile.destination.split("/").toList-templateFile.destination.split("/").last).foldLeft(currentPath){ (combinedString, newString) => 
          val folder = combinedString +"/"+ newString
          new File(folder).mkdir
          folder
        }
        
        val file = new File(currentPath+"/"+templateFile.destination)
        file.createNewFile
        val out = new BufferedWriter(new FileWriter(file));
        out.write(buffer.toString);
        out.close();
      } catch {
        case e: Exception => println(e) //@DEBUG
      }
	  }
    CommandResult("[success] Ran %s with arguments:\n - %s \n %s"
      .format(this.name, arguments.mkString("\n - "),bufferedFiles.mkString("\n")))
	}
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(argumentList: List[String]): CommandResult = CommandResult("Invkoed delete on template %s".format(this.name))

}