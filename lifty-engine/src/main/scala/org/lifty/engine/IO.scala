package org.lifty.engine

/* This file contains all of the code that is related to IO. Time will show if this
 * seperation makes sense at all.*/

object JLineInputComponent {
  def requestInput(msg: String) = SimpleReader.readLine(msg).getOrElse("")
}

object InputEmulatorComponent {
  def requestInput(msg: String) = "emulatedInput"
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