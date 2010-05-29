package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}
import org.fusesource.scalate._

import java.io.{StringWriter, PrintWriter}

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
	  val bufferedFiles = this.files.map{ file =>
	    val template = engine.load(file)
	    val buffer = new StringWriter()
      val context = new DefaultRenderContext(new PrintWriter(buffer))
      arguments.foreach{ argument => 
        context.attributes(argument.name) = argument.value
      }
      template.render(context)
      buffer.toString
	  }
    CommandResult("[success] Ran %s with arguments:\n - %s \n %s"
      .format(this.name, arguments.mkString("\n - "),bufferedFiles.mkString("\n")))
	}
}
trait Delete extends Operation {
	
	this: Template with Delete => 
	
	def delete(argumentList: List[String]): CommandResult = CommandResult("Invkoed delete on template %s".format(this.name))

}