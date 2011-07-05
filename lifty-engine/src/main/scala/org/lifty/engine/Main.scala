package org.lifty.engine

import java.io.File
import org.lifty.engine._

import scalaz._
import Scalaz._

/* This class is just used for debugging purposes */
object Main {

  def main(args: Array[String]): Unit = {

    LiftyInstance.run(args.toList).fold(
      e => println(e.message),
      s => println(s)
    )
  }
}
