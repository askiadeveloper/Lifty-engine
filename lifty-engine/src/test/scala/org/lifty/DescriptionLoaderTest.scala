package org.lifty.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File

import org.lifty.engine._

class DescriptionLoaderTest extends FlatSpec with ShouldMatchers {

  val descriptorEither = DescriptionLoader.load(Mocks.URI_TO_TEST_DESCRIPTION)

  "Parsing of test-descriptor.json" should "find 1 template" in  {
    descriptorEither.fold(
      err => err.message,
      descriptor => descriptor.templates.size
    ) should be === 1
  }

  it should "match fixture AST" in {
    descriptorEither.fold(
      err => err.message,
      descriptor => descriptor.templates
    ) should be === Mocks.templatesAST
  }

  "Trying to load a file that doesn't exist" should "Display an error message" in {
    DescriptionLoader.load(new File("xyzThisDoesntWorkAtAll").toURI.toURL).fold(
      err => true,
      descriptor => false
    ) should be === true
  }
}
