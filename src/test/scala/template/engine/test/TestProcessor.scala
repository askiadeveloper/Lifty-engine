package template.engine.test

import template.engine._
import net.liftweb.common.{Box, Empty, Failure, Full}


/**
* This is simply a test processer created to help me write my tests.
*/ 
object TestTemplateProcessor extends TemplateProcessor {
  
  def templates = TestTemplate :: TestTemplate2 :: Nil
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
  
  object testCommand1 extends Command {
    def keyword = "command1"
    def description ="command1 description"
    def run(arguments: List[String]) = CommandResult("ran TestCommand1")
  } 
  
  object testCommand2 extends Command {
    def keyword = "command2"
    def description ="command2 description"
    def run(arguments: List[String]) = CommandResult("ran TestCommand2")
  }
}