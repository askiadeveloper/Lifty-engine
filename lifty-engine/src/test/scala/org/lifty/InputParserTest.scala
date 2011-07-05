package org.lifty.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import java.io.{ File }

class InputParserTest extends FlatSpec with ShouldMatchers {

  val engineInstance = new LiftyEngineTestInstance(Mocks.URI_TO_TEST_DESCRIPTION)

  "Parsing input 'create x y z'" should "find the CreateCommand and the list x y z" in  {
    val arguments = "create" :: "x" :: "y" :: "z" :: Nil
    val expected = (CreateCommand, List("x","y","z"))
    engineInstance.parseCommand(arguments).either.right.get should be === expected
  }

  "Parsing input ''" should "result in an error" in  {
    val arguments = Nil
    engineInstance.parseCommand(arguments) === true
  }

  "Parsing input 'bogus input list'" should "result in an error" in  {
    val arguments = Nil
    engineInstance.parseCommand(arguments) === true
  }

  "parseTemplate with input 'snippet x y" should "find the Snippet template and the list x y" in {
    val arguments = "snippet" :: "x" :: "y" :: Nil
    val expected = (Mocks.templatesAST(0), List("x","y"))
    engineInstance.parseTemplate(arguments).either.right.get should be === expected
  }

  "parseTemplate with input 'bogus x y" should "shouldn't find any templates" in {
    val arguments = "bogus" :: "x" :: "y" :: Nil
    engineInstance.parseTemplate(arguments).isFailure should be === true
  }

  "parseArguments given the snippet templat and the input 'myname mypackage'" should "succeed" in  {
    val arguments = "myname" :: "mypackage" :: Nil
    val (template, rest) = engineInstance.parseTemplate("snippet" :: "x" :: "y" :: Nil).fold(
      err => throw new Exception("This shouldn't happen"), // TODO - really, an exception?
      success => success)

    val enviornment = engineInstance.parseArguments(template,arguments)
    val env = Environment(template, Map("snippetName" -> "myname", "snippetpack" -> "mypackage"))

    enviornment.isSuccess should be === true
    enviornment.either.right.get should be === env
  }

  "parseArguments given the snippet template and no arguments" should "request input from the user" in {
    val arguments = Nil
    val (template, rest) = engineInstance.parseTemplate("snippet" :: arguments).fold(
      err => throw new Exception("This shouldn't happen"), // TODO - really, an exception?
      success => success)

    val enviornment = engineInstance.parseArguments(template,arguments)
    val env = Environment(template, Map("snippetName" -> "emulatedInput", "snippetpack" -> "emulatedInput"))

    enviornment.isSuccess should be === true
    enviornment.either.right.get should be === env
  }
}



