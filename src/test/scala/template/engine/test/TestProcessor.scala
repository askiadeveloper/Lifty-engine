package template.engine.test

import template.engine._
import template.util._
import template.engine.commands._
import net.liftweb.common.{Box, Empty, Failure, Full}
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
* This is simply a test processer created to help me write my tests.
*/ 
object TestTemplateProcessor extends StandAloneTemplateProcessor {
  
  def templates = TestTemplate :: TestTemplate2 :: TestTemplate3 :: Nil
  override def commands = (TestCommand1(this) :: TestCommand2(this) :: TestCommand3(this) :: Nil) ::: super.commands
  
  object TestTemplate2 extends Template with Create {
    def name = "TestTemplate2"
    def description = "description2"
    def arguments = Nil
    def files = Nil
  }
  
  object TestTemplate extends Template with Create {
    def name = "TestTemplate"
    def description = "description"
    def arguments = Argument("name") :: Nil
    def files = Nil
  }
  
  object TestTemplate3 extends Template with Create {
    def name = "TestTemplate3"
    def description = "description3"
    def arguments = {
      object name extends Argument("name")
      object arg extends Argument("repeatable") with Repeatable with Optional
      name :: arg :: Nil
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