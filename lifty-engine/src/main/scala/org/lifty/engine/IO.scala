package org.lifty.engine


import java.io.{ InputStreamReader, BufferedReader, FileWriter, File}
import java.net.{ URI }

/* This file contains all of the code that is related to IO. Time will show if this
 * separation makes sense at all.*/

object JLineInputComponent {
  def requestInput(msg: String) = SimpleReader.readLine(msg).getOrElse("")
}

object InputEmulatorComponent {
  def requestInput(msg: String) = "emulatedInput"
}

object TemplateDownloader {
  
  /*
    This will download a template stored a URI and store it locally in the folder
    {user.home}/.lifty/{filePath}
    
    @param uri      The URI to the template file to download
    @param filePath A string representing the relative to {user.home}/.lifty. It's expected
                    that the string uses / as a path separator as this will get converted to
                    the appropriate OS specific path separator in the method
  */
  def downloadTemplate(uri: URI, filePath: String): Unit = {
    val in = new BufferedReader(new InputStreamReader(uri.toURL.openStream))
    var line = in.readLine()
    var text = ""
    while (line != null) {
      text += line
      line = in.readLine()
    }
      
    val home = System.getProperty("user.home") 
    val location = home + File.separator + ".lifty"  + File.separator + filePath.replace('/',File.separator.charAt(0))
    
    val file = new File(location)
    file.getParentFile.mkdirs()
    file.createNewFile()
    
    val writer = new FileWriter(new File(location))
    writer.write(text)
    writer.flush()
    writer.close()
  }
}

/*
  This has been completely stolen from SBT. Didn't want to add a dependency just
  to be able to access these classes 
*/

import jline.ConsoleReader
abstract class JLine extends NotNull
{
  protected[this] val reader: ConsoleReader
  def readLine(prompt: String) = JLine.withJLine { unsynchronizedReadLine(prompt) }
  private[this] def unsynchronizedReadLine(prompt: String) =
    reader.readLine(prompt) match
    {
      case null => None
      case x => Some(x.trim)
    }
}
private object JLine
{
  def terminal = jline.Terminal.getTerminal
  def createReader() =
    terminal.synchronized
    {
      val cr = new ConsoleReader
      terminal.enableEcho()
      cr.setBellEnabled(false)
      cr
    }
  def withJLine[T](action: => T): T =
  {
    val t = terminal
    t.synchronized
    {
      t.disableEcho()
      try { action }
      finally { t.enableEcho() }
    }
  }
}
object SimpleReader extends JLine
{
  protected[this] val reader = JLine.createReader()
}