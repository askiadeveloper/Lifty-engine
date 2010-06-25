import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestCommandParsing extends FlatSpec with ShouldMatchers {

  /* fixtures
  --------------------------------------------- */
  
  val unknownCommand = "fakeCommand"
  
  object testCommand1 extends Command {
    def keyword = "command1"
    def run(arguments: List[String]) = CommandResult("ran TestCommand1")
  } 
  
  object testCommand2 extends Command {
    def keyword = "command2"
    def run(arguments: List[String]) = CommandResult("ran TestCommand2")
  }
  
  object TestTemplateProcessor extends TemplateProcessor {
    
    def templates = Nil
    override def commands = testCommand1 :: testCommand2 :: Nil
    
  }
  
  /* let the testing begin
  --------------------------------------------- */
  
  "Known command" should "be accepted" in {
    val box = TestTemplateProcessor.resolveCommand(testCommand1.keyword)
    
    box.open_! should be === testCommand1
  }
  
  "Unknown command" should "not be accepted" in {
    val box = TestTemplateProcessor.resolveCommand(unknownCommand)
    val errorMsg = box.asInstanceOf[Failure].msg
    
    box.isInstanceOf[Failure] should be === true
    errorMsg should be === "[error] Command is not supported"
  }
  
}