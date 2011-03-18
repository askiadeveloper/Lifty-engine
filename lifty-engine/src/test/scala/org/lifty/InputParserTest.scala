package test.scala.org.lifty

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.lifty.engine._

import java.io.{ File }

class InputParserTest extends FlatSpec with ShouldMatchers {
  
  
  val description = DescriptionLoader.load(new File("src/test/resources/test-descriptor.json").toURI.toURL).right.get
  
  "Parsing input 'create x y z'" should "find the CreateCommand and the list x y z" in  {
    val arguments = "create" :: "x" :: "y" :: "z" :: Nil
    val expected = (CreateCommand, List("x","y","z"))
    InputParser.parseCommand(description,arguments).right.get should be === expected
  }
  
  "Parsing input ''" should "result in an error" in  {
    val arguments = Nil
    InputParser.parseCommand(description,arguments).isLeft === true
  }
  
  "Parsing input 'bogus input list'" should "result in an error" in  {
    val arguments = Nil
    InputParser.parseCommand(description,arguments).isLeft === true
  }
  
  "parseTemplate with input 'snippet x y" should "find the Snippet template and the list x y" in {
    val arguments = "snippet" :: "x" :: "y" :: Nil
    val expected = (Mocks.templatesAST(0), List("x","y"))
    InputParser.parseTemplate(description,arguments).right.get should be === expected
  }
  
  "parseTemplate with input 'bogus x y" should "shouldn't find any templates" in {
    val arguments = "bogus" :: "x" :: "y" :: Nil
    InputParser.parseTemplate(description,arguments).isLeft should be === true
  }
  
  "parseArguments given the snippet template, a description and the input 'myname mypackage'" should "succeed" in  {
    val arguments = "myname" :: "mypackage" :: Nil
    val (template, rest) = InputParser.parseTemplate(description,"snippet" :: "x" :: "y" :: Nil).right.get
    val env = Environment(template, Map("snippetName" -> "myname", "snippetpack" -> "mypackage"))
    InputParser.parseArguments(description, template, arguments).right.get should be === env
  }
  
  
  
}



