package org.lifty.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.lifty.engine._
import org.lifty.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestCommandParsing extends FlatSpec with ShouldMatchers {
  
  val unknownCommand = "fakeCommand"
  
  val testCommand1 = TestTemplateProcessor.commands(0)
  val testCommand2 = TestTemplateProcessor.commands(1)
  
  "Known command" should "be accepted" in {
    val box = TestTemplateProcessor.resolveCommand(testCommand1.keyword)
    
    box.open_! should be === testCommand1
  }
  
  
}