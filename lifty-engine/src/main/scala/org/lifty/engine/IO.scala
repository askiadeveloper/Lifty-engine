package org.lifty.engine

import org.lifty.engine._ 

/*
  At some point I have to figure out how to use the cake-pattern. Potentially 
  I might have to create the appropriate InputReader and pass it along to all
  methods using the ReaderMonad. This way the actual application can use a 
  real input service and the tests can use a fake one. 
*/

trait InputReader {
  def readLine(message: String): String
}

object IO {
  
  def requestInputForArgument(argument: Argument) = "\n"
  
}