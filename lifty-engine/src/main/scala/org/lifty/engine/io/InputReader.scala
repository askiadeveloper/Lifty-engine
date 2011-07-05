// 
//  InputReader.scala
//  Lifty Engine
//  
//  Created by Mads Hartmann Jensen on 2011-07-04.
// 

package org.lifty.engine.io

import scalaz._
import scalaz.effects._
import Scalaz._
import java.io.{ InputStreamReader, BufferedReader, FileWriter, File}
import java.net.{ URI }
import jline.ConsoleReader

// requests input from the user using jline 
object InputReaderComponent {
  def requestInput(msg: String): IO[String] = io {
    InputReader.readLine(msg).getOrElse("")
  } 
}

// always returns IO["emulatedInput"]
object EmulatedInputReaderComponent {
  def requestInput(msg: String): IO[String] = io {
    "emulatedInput"
  }
}

/*
 * This has been completely stolen from SBT. Didn't want to add a dependency just
 * to be able to access these classes. 
*/

private abstract class JLine extends NotNull
{
  protected[this] val reader: ConsoleReader
  
  def readLine(prompt: String) = JLine.withJLine { unsynchronizedReadLine(prompt) }
  
  private[this] def unsynchronizedReadLine(prompt: String) = reader.readLine(prompt) match {
    case null => None
    case x => Some(x.trim)
  }
}

private object JLine {

  def terminal = jline.Terminal.getTerminal

  def createReader() = terminal.synchronized {
    val cr = new ConsoleReader
    terminal.enableEcho()
    cr.setBellEnabled(false)
    cr
  }

  def withJLine[T](action: => T): T = {
    val t = terminal
    t.synchronized {
      t.disableEcho()
      try { action }
      finally { t.enableEcho() }
    }
  }
}

private object InputReader extends JLine {
  protected[this] val reader = JLine.createReader()
}
