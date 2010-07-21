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
  
  "Dependent template" should "depend on snippet" in {
    TestTemplateProcessor.processInput("create dependent Name my.package")
    val f = new File("src/test/output/snippet.scala")
    f.exists should be === true
    f.delete
  }
  
  
}