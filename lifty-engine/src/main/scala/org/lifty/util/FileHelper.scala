package org.lifty.util

import java.io.{File, BufferedWriter, FileWriter, FileInputStream, InputStreamReader, BufferedReader}
import org.lifty.engine._

/**
* Object to help dealing with the annoyance of dealing with 
* files inside/outside jars
*/
object FileHelper {
  
  /**
  * This methods searches the files and subfolders (recursively) for a file
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
  * Takes a RELATIVE path to a file and creates every folder in the path
  * before the file.
  * 
  * @param  path  The path to creates folders from
  */
  def createFolderStructure(path: String) {
    val currentPath = new File("").getAbsolutePath
    val dirpath = currentPath + File.separator + (path.split(File.separator.toCharArray.toList.head)
      .toList-path.split(File.separator.toCharArray.toList.head).last)
      .mkString(File.separator)
    new File(dirpath).mkdirs
  }
  
  /**
  * Loads a file from a string with the path to the file. It will copy the original file 
  * into a new one with the prefix _temp_ and return that file.
  * 
  * @param  path  well isn't it obvious
  * @return       dunno
  */
  def loadFile(path: String): File = {
    
    val tempFileName: String = {
      if      (path.contains("/"))  "_temp_"+path.split("/").last
      else if (path.contains('\\')) "_temp_"+path.split('\\').last
      else                          "_temp_"+path
    }
    
    val resourcePath: String = { java.io.File.separator match {
      case "/"  => path
      case "\\" => {
        path.replace('\\','/') match {
          case p if p.charAt(1) == ':' => p.slice(2,p.length)
          case p => p
      }}
    }}     

    val is = if (!GlobalConfiguration.runningAsJar) { // we're not running as a jar.
      new FileInputStream(new File(path))
    } else {
      // the path for a resource inside a jar is a path with / separations
      this.getClass().getResourceAsStream(resourcePath) 
    }
    if (is != null) {
      val file = new File(tempFileName)
      createFolderStructure(file.getPath)
      file.createNewFile
      val out = new BufferedWriter(new FileWriter(file));
      val txt = readContentsOfFile(is) 
      out.write(txt)
      out.close
      file
    } else {
      throw new Exception("FileHelper wasn't able to load resource with path %s from the jar".format(resourcePath))
    }
  }
  
  def readContentsOfFile(inputStream: java.io.InputStream): String = {
    val in = new BufferedReader(new InputStreamReader(inputStream))
    var lines: List[String] = Nil
    var line = in.readLine()
    while (line != null) {
      lines = lines ::: List(line)
      line = in.readLine()
    }
    in.close()
    lines.mkString("\n")
  } 


}
