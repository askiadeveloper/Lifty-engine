package template.util

import java.io.{File, BufferedWriter, FileWriter}
import template.engine._

/**
* Object to help dealing with the annoyance of dealing with 
* files inside/outside jars
*/
object FileHelper {
  
  /**
  * This methods searches the files and subfolders (recursivly) for a file
  * with the name specified
  * 
  * @param  dir   The directory to search through
  * @param  name  The name of the file to search for
  * @return       Empty if it couldn't find the file, otherwise Full(file)
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
  
  /**
  * To delete a folder with java it has to be empty, so this
  * deletes every subfolder & files of a java.io.File and ends 
  * with deleting the file itself.    
  * 
  * @param  file  File to delete.
  */
  def recursiveDelete(file: File): Unit = {
    if (file.isDirectory) {
      file.list.toList.foreach{ path => recursiveDelete(new File(file,path)) }
    } 
    file.delete
  }
  
  /**
  * Takes a path and creates every folder in the path
  * 
  * @param  path  The path to creates folders from
  */
  def createFolderStructure(path: String) {
    val currentPath = new File("").getAbsolutePath
    (path.split("/").toList-path.split("/").last).foldLeft(currentPath){ (combinedString, newString) => 
      val folder = combinedString +"/"+ newString
      new File(folder).mkdir
      folder
    }
  }
  
  /**
  * Loads a file from a string with the path to the file. If the processor 
  * is running as a jar (i.e. it's not running in test mode) it will copy
  * the file from the jar into a temp file and return that.
  * 
  * @param  path  well isn't it obvious
  * @return       dunno
  */
  def loadFile(path: String): File = {
    if (!GlobalConfiguration.runningAsJar) { // we're not running as a jar.
      new File(path)
    } else {
      val tempFileName = "_temp_"+path.split("/").last
      try {
        val is = this.getClass().getResourceAsStream(path) 
        val in = scala.io.Source.fromInputStream(is)
        val file = new File(tempFileName)
        createFolderStructure(file.getAbsolutePath)
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
  
}
