import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestCommands extends FlatSpec with ShouldMatchers {
  
  object TestTemplate2 extends Template with Create {
    def name = "TestTemplate2"
    def arguments = Nil
    def files = Nil
  }
  
  object TestTemplate extends Template with Create {
    def name = "TestTemplate"
    def arguments = Nil
    def files = Nil
  }
  
  object TestTemplateProcessor extends TemplateProcessor {
    
    def templates = TestTemplate :: TestTemplate2 :: Nil

  }

  "The help command" should "should list all of the comands" in {
    val command = TestTemplateProcessor.resolveCommand("help")
    val msg = command.open_!.run(Nil).message
    msg should be === "create\ndelete\ntemplates\nhelp"
  }
  
  "The templates command" should "should list all of the templates" in {
    val command = TestTemplateProcessor.resolveCommand("templates")
    val msg = command.open_!.run(Nil).message
    msg should be === TestTemplate.name + "\n" + TestTemplate2.name
  }
  
}