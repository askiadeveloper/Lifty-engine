package template.util

import java.io.{BufferedReader, InputStreamReader}
import sbt._

object IOHelper {
  
  def requestInput(message: String): String = {

    SimpleReader.readLine("[info] " + message) match {
      case Some(str) => str
      case None => ""
    }
  }
  
}