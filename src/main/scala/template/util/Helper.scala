package template.util

import template.engine.{ArgumentResult, GlobalConfiguration}
import java.io._
import java.util.{Enumeration}
import java.util.jar._
import net.liftweb.common.{Box, Empty, Failure, Full}

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
		println("writing %s to %s".format(resource, destFile))
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
				
		val tempFile = createTempFile(from)
		try {
			val is = new FileInputStream(tempFile)
			val in = scala.io.Source.fromInputStream(is)
			val toFile = new File(to)
			toFile.createNewFile
			val out = new BufferedWriter(new FileWriter(toFile));
			in.getLines.foreach(out.write(_))
			out.close
		} catch {
			case e: Exception => e.printStackTrace
		} finally {
		  tempFile.delete
		}
	}
	
	def createTempFile(path: String): File = {
	  if (!GlobalConfiguration.runningAsJar) { // we're not running as a jar.
			new File(path)
		} else {
			val tempFileName = "_temp_"+path.split("/").last
			try {
				val is = this.getClass().getResourceAsStream(path) 
				val in = scala.io.Source.fromInputStream(is)
				val file = new File(tempFileName)
				file.createNewFile
				val out = new BufferedWriter(new FileWriter(file));
				in.getLines.foreach{ line => out.write(line) }
				out.close
				file
			} catch {
				case e: Exception => {
				  println("debugging: " + path)
          e.printStackTrace
					new File(tempFileName)
				}
			}
		}
	}

	
	/**
	* This methods searches the files and subfolders (recursivly) for a file
	* with the name specified
	* 
	* @param	dir 	The directory to search through
	* @param	name	The name of the file to search for
	* @return	    	Empty if it couldn't find the file, otherwise Full(file)
	*/
	def findFileInDir(dir :File, name: String): Option[File] = {
		def recursiveListFiles(f: File): List[File] = {
		  val these = f.listFiles.toList
		  these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
		}
		val list = recursiveListFiles(dir)
		list.filter( _.getName == name ) match {
			case Nil => None
			case file :: rest => Some(file)
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