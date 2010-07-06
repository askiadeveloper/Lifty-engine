package template.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import template.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestCommands extends FlatSpec with ShouldMatchers {
  
  val testTemplate = TestTemplateProcessor.templates(0)
  val testTemplate2 = TestTemplateProcessor.templates(1)
  
  "The help command" should "should list all of the comands" in {
    val command = TestTemplateProcessor.resolveCommand("help")
    val msg = command.open_!.run(Nil).message
    val output = {
      val longestCommandName = TestTemplateProcessor.commands.map( _.keyword.length).reduceLeft(_ max _)
      (TestTemplateProcessor.commands.map{ cmd => 
        val spaces = (for (i <- 0 to longestCommandName-cmd.keyword.length ) yield " ").mkString("")
        "   %s%s   %s".format(cmd.keyword, spaces, cmd.description)
      }).mkString("\n")
    }
    
    msg should be === output
  }
  
  "The templates command" should "should list all of the templates" in {
    val command = TestTemplateProcessor.resolveCommand("templates")
    val msg = command.open_!.run(Nil).message
    val output = {
      val longestTemplateName = TestTemplateProcessor.templates.map(_.name.length).reduceLeft(_ max _)
      (TestTemplateProcessor.templates.map{ template => 
        val spaces = (for (i <- 0 to longestTemplateName-template.name.length) yield " ").mkString("")
        val arguments = template.arguments.map(_.name).mkString(",")
        "   %s%s   %s".format(template.name, spaces, arguments)
      }).mkString("\n")
    }
    msg should be === output
  }
  
}