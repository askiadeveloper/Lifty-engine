package org.lifty.util

import java.io._
import java.util.jar._
import java.util.{Enumeration}
import net.liftweb.common.{Box, Empty, Failure, Full}
import org.lifty.engine.{ArgumentResult, GlobalConfiguration}

/**
* This object is used to help with gerneral you might need when writig your templates
* stuff like copying files that doesn't need processing, creating a folder structure.
*/
object TemplateHelper {

  /**
  * Takes a package name and calculates the path. I.e. com/sidewayscoding
  * would return com.sidewayscoding
  * 
  * @param  thePackage  the package to convert
  * @return             a path formatted string
  */
  def pathOfPackage(thePackage :String) = thePackage.replace(".",File.separator )
  
  
  /**
  * Takes a path and calculates the package of it. i.e. com.sidewayscoding
  * would return com/sidewayscoding
  * 
  * @param  path  well isn't it obvious
  * @return       dunno
  */
  def packageOfPath(path :String) = path.replace("src/main/scala/","").replace(File.separator,".")
 
  /**
  * Takes any number of strings (paths) and creates a directory for each
  * string. The strings can contain arguments like so path/to/my/{package}/
  * and package will get replaced with the value parsed for the argument.
  * The argument has to be specified by the template or it won't work
  * 
  * @param  arguments the templates argument
  * @param  paths     Paths to all the folders you want to create
  */
  def createFolderStructure(arguments: List[ArgumentResult])(paths: String*): Unit = {
    paths.foreach{ path => 
      new File(replaceVariablesInPath(path, arguments)).mkdirs 
    }
  }

  /**
  * Copies a file without doing any processing to the given location. You can
  * only process files not folders. It only copies the files it there isn't 
  * allready a file at the destination.
  * 
  * @param  from  The file to copy
  * @param  to    The destination of the file
  */
  def copy(from: String, to:String): Boolean = {
    
    val currentPath = new File("").getAbsolutePath // TODO: Not sure this is needed.
    val toFile = new File(convertToOSSpecificPath(to))
    val tempFile = FileHelper.loadFile(from)

    try {
      if (IOHelper.safeToCreateFile(toFile)){
        val is = new FileInputStream(tempFile)
        FileHelper.createFolderStructure(toFile.getPath)
        if (toFile.createNewFile == true) {
          val out = new BufferedWriter(new FileWriter(toFile));
          out.write(FileHelper.readContentsOfFile(is))
          out.close
          true
        } else { throw new Exception("Wasn't able to create a new file for: %s".format(toFile.getPath)) }
      } else false
    } catch {
      case e: Exception => 
        println("Wasn't able to copy file from %s to %s".format(tempFile.getPath, toFile.getPath))
        e.printStackTrace
      false
    } finally {
      tempFile.delete 
    }
  }

  /**
  * looks through the path string for any variables (i.e ${someVal}) and replaces
  * it with the acctual value passed to the operation
  * 
  * @param  path      the path to replace variables in
  * @param  arguments the arguments to search through for variables to replace
  * @return           A string with the variables replaced
  */
  def replaceVariablesInPath(path: String, arguments: List[ArgumentResult]): String = {
    def findAndTransformValueForArgument(name: String): String = {
      try {
        arguments.filter( _.argument.name == name ).first.pathValue
      } catch {
        case e: Exception => e.printStackTrace; "failure"
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
  
  def convertToOSSpecificPath(path: String) = {
     java.io.File.separator match {
      case "/"  => path
     	case "\\" => path.replace("/","""\\""") 
    }
  }
  
  def printablePath(path: String) = {
    java.io.File.separator match {
      case "/"  => path
     	case "\\" => path.replace("/","\\") 
    }
  }
  
}
