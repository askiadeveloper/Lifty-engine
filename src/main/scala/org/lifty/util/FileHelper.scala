package org.lifty.util

import java.io.{File, BufferedWriter, FileWriter, FileInputStream}
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
    val dirpath = currentPath + File.separator + (path.split(File.separator)
      .toList-path.split(File.separator).last)
      .mkString(File.separator)
    new File(dirpath).mkdirs
  }
  
  /**
  * Loads a file from a string with the path to the file. It will copy the original file 
  * into a new one with the prefix _temp_ and return that file.
  * If the first char is a / it will convert it to the OS specific root.
  * 
  * @param  path  well isn't it obvious
  * @return       dunno
  */
  def loadFile(path: String): File = {
    val tempFileName = "_temp_"+path.split(File.separator).last
    val safePath = path.toCharArray.toList match {
      case arr if arr.head == '/' => OSSpecificRoot + arr.slice(1,arr.size-1).mkString("")
      case arr => arr.mkString("")
    }
    try {
      val is = if (!GlobalConfiguration.runningAsJar) { // we're not running as a jar.
        new FileInputStream(new File(path))
      } else {
        this.getClass().getResourceAsStream(path) 
      }
      val in = scala.io.Source.fromInputStream(is)
      val file = new File(tempFileName)
      createFolderStructure(file.getPath)
      file.createNewFile
      val out = new BufferedWriter(new FileWriter(file));
      in.getLines.foreach{ line => out.write(line) }
      out.close
      file
    } catch {
      case e: Exception => {
        e.printStackTrace
        new File(tempFileName)
      }
    }
  }
  
  def OSSpecificRoot: String = {
    File.separator match {
      case """/""" => """/"""
      case """\""" => """\\\\"""
    }
  }
  
}
