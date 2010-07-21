package template.engine.test

import template.engine._
import template.util._
import template.engine.TemplateFile
import template.engine.commands._
import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
* This is simply a test processer created to help me write my tests.
*/ 
object TestTemplateProcessor extends StandAloneTemplateProcessor {
  
  def templates = TestTemplate :: Snippet :: TestTemplate3 :: Nil
  override def commands = (TestCommand1(this) :: TestCommand2(this) :: TestCommand3(this) :: Nil) ::: super.commands
    
  object TestTemplate extends Template with Create {
    def name = "TestTemplate"
    def description = "description"
    def arguments = Argument("name") :: Nil
    def files = Nil
  }
  
  object Snippet extends Template with Create {
    def name = "snippet"
    def description = "test snippet"
    def arguments = Argument("name") :: Argument("pack") :: Nil
    def files = List(TemplateFile(
      "src/test/resources/snippet.ssp","src/test/output/snippet.scala"
    ))
  }
  
  // not used in the tests, just in the sbt console to verify it works
  object TestTemplate3 extends Template with Create {
    def name = "TestTemplate4"
    def description = "description4"
    def arguments = {
      object arg1 extends Argument("arg1") with Default with Value{ value = Empty }
      object arg2 extends Argument("arg2") with Default with Value{ value = Full("test") }
      object arg3 extends Argument("arg3")
      object arg4 extends Argument("arg4") with Optional
      arg1 :: arg2 :: arg3 :: arg4 :: Nil
    }
    def files = Nil
  }
  
  case class TestCommand1(processor: TemplateProcessor) extends Command {
    def keyword = "command1"
    def description ="command1 description"
    def run(arguments: List[String]) = Full(CommandResult("ran TestCommand1"))
  } 
  
  case class TestCommand2(processor: TemplateProcessor) extends Command {
    def keyword = "command2"
    def description ="command2 description"
    def run(arguments: List[String]) = Full(CommandResult("ran TestCommand2"))
  }
  
  case class TestCommand3(processor: TemplateProcessor) extends Command {
    def keyword = "command3"
    def description ="command3 description"
    def run(arguments: List[String]) = {
      val line = IOHelper.requestInput("Type something: ")
      Full(CommandResult("you typed: %s".format(line)))
    }
  }
}