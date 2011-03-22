package org.lifty

import java.net.{ URL }
import org.lifty.engine._
import sbt._
import Keys._

object Lifty extends Plugin {

  override lazy val settings = Seq(commands += liftyCommand)

  lazy val liftyCommand =
    Command.args("lifty","<help>") { (state, args) =>
      val url = new File("lifty-engine/src/test/resources/test-descriptor.json").toURI.toURL
      val liftyEngineInstance = new LiftyEngineInstance(url)
      liftyEngineInstance.run(args).fold(
        e => println(e.message),
        s => println(s)
      )
      state
    }

}
