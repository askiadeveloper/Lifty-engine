package org.lifty.engine

import java.io.File
import org.lifty.engine._

import scalaz._
import Scalaz._

/* This class is just used for debugging purposes */
object Main {

  def main(args: Array[String]): Unit = {

    val url = new File("lifty-engine/src/test/resources/test-descriptor.json").toURI.toURL
    val liftyEngineInstance = new LiftyEngineInstance(url)

    liftyEngineInstance.run(args.toList).fold(
      e => println(e.message),
      s => println(s)
    )
  }
}
