package org.lifty.engine

/* This file contains all of the code that is related to IO. Time will show if this
 * seperation makes sense at all.*/

object JLineInputComponent {
  def requestInput(msg: String) = "...." // TODO: Use JLine for this
}

object InputEmulatorComponent {
  def requestInput(msg: String) = "emulatedInput"
}
