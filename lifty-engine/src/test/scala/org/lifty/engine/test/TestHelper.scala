package org.lifty.engine.test

import java.io.File

object TestHelper {
  def makeProperPath(paths: List[String]) = paths.mkString(File.separator)
}