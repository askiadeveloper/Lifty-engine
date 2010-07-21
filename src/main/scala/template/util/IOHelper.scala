package template.util

import java.io.{BufferedReader, InputStreamReader, File}
import sbt._

object IOHelper {
  
  def safeToCreateFile(file: File): Boolean = {
    
    def askUser: Boolean = {
      val question = "The file %s exists, do you want to override it? (y/n): ".format(file.getPath)
      IOHelper.requestInput(question) match {
        case "y" => true
        case "n" => false
        case _ => askUser
      }
    }
    
    if (file.exists) askUser else true
  }
  
  def requestInput(message: String): String = {

    SimpleReader.readLine("[info] " + message) match {
      case Some(str) => str
      case None => ""
    }
  }
  
}