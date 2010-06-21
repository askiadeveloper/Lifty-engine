package template.util

import template.engine.{ArgumentResult, GlobalConfiguration}
import java.io._
import java.util.{Enumeration}
import java.util.jar._

/** 
	GOT THE FOLLOWING CLASS AND IMPLICIT DEF FROM:  
	http://johlrogge.wordpress.com/2008/09/11/scala-nugget-implicits-to-make-java-blend-into-scala/ 
**/

class RichEnumeration[T](enumeration:Enumeration[T]) extends Iterator[T] {
  def hasNext:Boolean =  enumeration.hasMoreElements()
  def next:T = enumeration.nextElement()
}

object Helper {

	implicit def enumerationToRichEnumeration[T](enumeration:java.util.Enumeration[T]):RichEnumeration[T] = {
		new RichEnumeration(enumeration)
	}
  
	def pathOfPackage(thePackage :String) = thePackage.replace(".","/")
	def packageOfPath(path :String) = path.replace("src/main/scala/","").replace("/",".")
 
	// Takes any number of strings (paths) and creates a directory for each
	// string
	def createFolderStructure(arguments: List[ArgumentResult])(paths: String*): Unit = {
		paths.foreach{ path => 
			new File(replaceVariablesInPath(path, arguments)).mkdirs 
		}
	}


	def writeResourceTofile(resource: String, destFile: File) = {
		try {
			val is = this.getClass().getResourceAsStream(resource) 
			val in = scala.io.Source.fromInputStream(is)
			destFile.getParentFile.mkdirs
			destFile.createNewFile
			val out = new BufferedWriter(new FileWriter(destFile));
			in.getLines.foreach(out.write(_))
			out.close
		} catch {
			case e: Exception => println(e)
		} 
	}


	def copy(from: String, to:String): Unit = {
		def copyfiles(from: File, to:File): Unit = {
			if(from.isDirectory) {
				if (!to.exists) to.mkdirs
				from.list.toList.foreach{ file => 
					copyfiles(new File(from, file), new File(to, file))
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
		
		if(!GlobalConfiguration.runningAsJar) {
			copyfiles(new File(from), new File(to))
		} else {
			val pathToJar = this.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath
			val jar = new JarFile(pathToJar)
			// get the appropriate files form the jar
			jar.entries
				.map{ entry => "/"+entry.getName}
				.filter{ entry => entry.contains(from) }
				.foreach{ entry => 
					val destionation = entry.replace(from,to)
					writeResourceTofile(entry, new File(destionation))
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