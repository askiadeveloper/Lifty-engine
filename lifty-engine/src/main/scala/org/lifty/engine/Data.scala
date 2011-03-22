package org.lifty.engine

/*
  The following are the data classes that are used internally
*/

trait Command                           { val keyword: String }
object HelpCommand      extends Command { val keyword = "help"}
object CreateCommand    extends Command { val keyword = "create"}
object TemplatesCommand extends Command { val keyword = "templates"}

case class Error(
  message:          String)

case class Environment(
  template:         Template,
  values:           Map[String,String])

/*
  The following are the case classes needed to describe a Lifty-engine application
  in JSON
*/

case class Description(
  repository:     String,
  templates:      List[Template])

case class Template(
  name:           String,
  description:    String,
  notice:         Option[String],
  arguments:      List[Argument],
  files:          List[TemplateFile],
  injections:     List[TemplateInjection],
  dependencies:   List[TemplateReference])

case class TemplateFile(
  file:           String,   // The scalate template to render
  destination:    String    // Where to create a file with the result
)

case class Argument(
  name:         String,
  default:      Option[String],
  optional:     Option[Boolean],
  repeatable:   Option[Boolean])

case class TemplateInjection(
  file:         String,
  into:         String,
  point:        String
)

case class TemplateReference(
  name:         String
)
