package org.lifty.engine

import java.io.{ File }

object Mocks {

  val URI_TO_TEST_DESCRIPTION = new File("lifty-engine/src/test/resources/test-descriptor.json").toURI.toURL

  /*
    This is the templates portion of the AST that I would expect when reading
    the test-descriptor.json file.
  */
  val templatesAST = List(Template(
    "snippet",
    "creates a snippet",
    None,
    List(
      Argument("snippetName",None,None,None),
      Argument("snippetpack",Some("{package}"),None,None)
    ),
    List(
      TemplateFile("snippet.ssp","src/main/scala/${snippetpack}/${snippetName}.scala")
    ),
    List(),
    List()
  ))

}

