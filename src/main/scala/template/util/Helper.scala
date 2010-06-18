package template.util

import template.engine.{ArgumentResult}
import java.io.{File}
import java.io.{StringWriter, PrintWriter, File, BufferedWriter, FileWriter, FileInputStream, FileOutputStream}

object Helper {
  
  def pathOfPackage(thePackage :String) = thePackage.replace(".","/")
  def packageOfPath(path :String) = path.replace("src/main/scala/","").replace("/",".")
 
	// Takes any number of strings (paths) and creates a directory for each
	// string
	def createFolderStructure(arguments: List[ArgumentResult])(paths: String*): Unit = {
		paths.foreach{ path => 
			new File(replaceVariablesInPath(path, arguments)).mkdirs 
		}
	}


	/**
	* This methods copies all the files inside the from folder to the to folder
	* 
	* @param	from	path to the folder you want to copy
	* @param	to  	path to the folder where you want the fodler to end up
	*/
	def copy(from: File, to:File): Unit = {
		
		if(from.isDirectory) {
			if (!to.exists) to.mkdirs
			from.list.toList.foreach{ file => 
				copy(new File(from, file), new File(to, file))
			}
		} else {
			try {
				val is = new FileInputStream(from)
				val in = scala.io.Source.fromInputStream(is)
				val out = new BufferedWriter(new FileWriter(to));
				in.getLines.foreach(out.write(_))
				out.close
			} catch {
				case e: Exception => // mmm, just swallowed an exception!
			}
		}
	}

	// looks through the path string for any variables (i.e ${someVal}) and replaces
	// it with the acctual value passed to the operation
	
	def replaceVariablesInPath(path: String, arguments: List[ArgumentResult]): String = {
		def findAndTransformValueForArgument(name: String): String = {
			try {
				arguments.filter( _.argument.name == name ).first.pathValue
			} catch {
				case e: Exception => println(e);""
			}
		}
		
		var newPath = path
		"""\$\{\w*\}""".r.findAllIn(path).toList match {
			case list if !list.isEmpty => {
				list.map(_.toString).foreach{ variable => 
					val argName = variable.replace("${","").replace("}","") //TODO: make prettier?
					newPath = newPath.replace(variable,findAndTransformValueForArgument(argName))
				}
				newPath
			}
			case _ => path
		}
	}
	
}