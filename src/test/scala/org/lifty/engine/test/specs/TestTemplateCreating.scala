package org.lifty.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import org.lifty.engine._
import org.lifty.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestTemplateCreating extends FlatSpec with ShouldMatchers {
  
  "Snippet template" should "be created" in {
    TestTemplateProcessor.processInput("create snippet Name my.package")
    val f1 = new File("src/test/output/snippet.scala")
    f1.exists should be === true
    f1.delete
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
    val f3 = new File("src/test/output/index.html")
    f1.exists should be === true
    f2.exists should be === true
    f3.exists should be === true
    f1.delete
    f2.delete
    f3.delete
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
 
 "create snippet with index mypackage" should "create both templates" in  {
   val v = TestTemplateProcessor.processInput("create snippet with index mypackage arg")
   val f1 = new File("src/test/output/snippet.scala")
   val f3 = new File("src/test/output/index.html")
   f1.exists should be === true
   f3.exists should be === true
   f1.delete
   f3.delete
 }

 "create snippet with index with empty mypackage" should "create both templates" in  {
   val v = TestTemplateProcessor.processInput("create snippet with index with empty somevalforempy mypackage name ")
   val f1 = new File("src/test/output/snippet.scala")
   val f2 = new File("src/test/output/empty.html")
   val f3 = new File("src/test/output/index.html")
   f1.exists should be === true
   f2.exists should be === true
   f3.exists should be === true
   f1.delete
   f2.delete
   f3.delete
 }
 
 // "test" should "test" in {
 //   println(TestTemplateProcessor.processInput("create user"))
 //   true should be === true
 // }

  
}