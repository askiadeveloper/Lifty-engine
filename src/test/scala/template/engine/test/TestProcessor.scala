package template.engine.test

import template.engine._
import net.liftweb.common.{Box, Empty, Failure, Full}


/**
* This is simply a test processer created to help me write my tests.
*/ 
object TestTemplateProcessor extends StandAloneTemplateProcessor {
  
  def templates = TestTemplate :: TestTemplate2 :: TestTemplate3 :: Nil
  override def commands = (testCommand1 :: testCommand2 :: Nil) ::: super.commands
  
  object TestTemplate2 extends Template with Create {
    def name = "TestTemplate2"
    def arguments = Nil
    def files = Nil
  }
  
  object TestTemplate extends Template with Create {
    def name = "TestTemplate"
    def arguments = Argument("name") :: Nil
    def files = Nil
  }
  
  object TestTemplate3 extends Template with Create {
    def name = "TestTemplate3"
    def arguments = {
      object name extends Argument("name")
      object arg extends Argument("repeatable") with Repeatable with Optional
      name :: arg :: Nil
    }
    def files = Nil
  }
  
  object testCommand1 extends Command {
    def keyword = "command1"
    def description ="command1 description"
    def run(arguments: List[String]) = Full(CommandResult("ran TestCommand1"))
  } 
  
  object testCommand2 extends Command {
    def keyword = "command2"
    def description ="command2 description"
    def run(arguments: List[String]) = Full(CommandResult("ran TestCommand2"))
  }
}