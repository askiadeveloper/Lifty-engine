package template.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import template.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestTemplateCreating extends FlatSpec with ShouldMatchers {
  
  "Snippet template" should "be created" in {
    TestTemplateProcessor.processInput("create snippet Name my.package")
    val f = new File("src/test/output/snippet.scala")
    f.exists should be === true
    f.delete
  }
 
  "Dependent template" should "depend on the files of itself and dependents (duplicates excluded)" in {
    val snippet = TestTemplateProcessor.templates.filter(_.name == "snippet").head
    val template = TestTemplateProcessor.templates.filter(_.name == "dependent").head
    val files = template.getAllFiles
    files should be === template.files // the snippet template in "dependent" takes precedence  
  }
  
  it should "create it's dependencies" in {
    TestTemplateProcessor.processInput("create dependent other name pack")
    val f1 = new File("src/test/output/snippet.scala")
    val f2 = new File("src/test/output/dependent.scala")
    f1.exists should be === true
    f2.exists should be === true
    f1.delete
    f2.delete
  }
  
  // it should "ask for input if not provided. Even for dependent arguments" in {
  //   TestTemplateProcessor.processInput("create dependent other name")
  //   val f1 = new File("src/test/output/snippet.scala")
  //   val f2 = new File("src/test/output/dependent.scala")
  //   f1.exists should be === true
  //   f2.exists should be === true
  //   f1.delete
  //   f2.delete
  // }
  
  it should "have the arguments of itself and dependents" in {
    val template = TestTemplateProcessor.templates.filter(_.name == "dependent").head
    val snippet = TestTemplateProcessor.templates.filter(_.name == "snippet").head
    template.getAllArguments should be === template.arguments ::: snippet.arguments
  }
 
  
  
}