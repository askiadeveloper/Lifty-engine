package org.lifty.engine

import scalaz._
import Scalaz._

/* This is the component of the LiftyEngineComponent that deals with the parsing of
 * the input from the user. */
trait InputParser {

  this: LiftyEngineComponent =>

  /*
   Given a list of arguments it will (if successful) return the appropriate Command
   and the rest of the arguments. Otherwise an instance of Error.
   */
  def parseCommand(arguments: List[String]): Validation[Error, (Command, List[String])] = {
    if (arguments.nonEmpty) {
      arguments.head match {
        case TemplatesCommand.keyword => (TemplatesCommand, arguments.tail).success
        case HelpCommand.keyword => (HelpCommand, arguments.tail).success
        case CreateCommand.keyword => (CreateCommand, arguments.tail).success
        case UpdateTemplatesCommand.keyword => (UpdateTemplatesCommand,Nil).success
        case command => Error("No command named %s".format(command)).fail
      }
    } else { Error("You have to supply an argument").fail }
  }

  /*
   Given a list of arguments it will (if successful) return the appropriate Template
   and the rest of the arguments. Otherwise an instance of Error
   */
  def parseTemplate(arguments: List[String]): Validation[Error, (Template, List[String])] = {
    if (arguments.nonEmpty) {
      (for {
        template <- description.templates.filter(_.name == arguments.head).headOption
      } yield (template, arguments.tail).success)
        .getOrElse(Error("No template with the name %s".format(arguments.head)).fail)
    } else { Error("You have to write the name of the template").fail }
  }

  /*
   Given a Description, a Template and a list of arguments it will create an Environment.
   */
  def parseArguments(template: Template,
    arguments: List[String]): Validation[Error, Environment] = {

    // request input for an argument that is missing
    val requestInputForMissingArgument = (argument: Argument) => {
      val value = this.inputComponent.requestInput("Enter value for %s: ".format(argument.name))
      (argument.name, value) // TODO: This is a side-effect. IO-Monad?
    }

    // map arguments to a string and swap defaults. Request input if no default exists
    val swapDefaults = (tuple: (Argument, String)) => {
      val (argument, value) = tuple
      if (value == "_") {
        requestInputForMissingArgument(argument)
      } else (argument.name, value)
    }

    // filter optional arguments
    val isOptional = (argument: Argument) => {
      !argument.optional.isEmpty && argument.optional.get == true
    }

    // pair the values passed with the arguments needed
    val pairs = template.arguments.zip(arguments)

    if (pairs.size == template.arguments.size) { // correct number of arguments
      Environment(template, Map(pairs.map(swapDefaults): _*)).success
    } else if (pairs.size < template.arguments.size) { // too few arguments. Request input
      val kvs = pairs.map(swapDefaults) :::
        template.arguments
        .slice(pairs.size, template.arguments.size) // get the missing arguments
        .filterNot(isOptional) // Don't care about the optional arguments here
        .map(requestInputForMissingArgument) // request input for the ones that are missing
      Environment(template, Map(kvs: _*)).success
    } else { // Too many. Deal with repeatable arguments
      Environment(template, Map(pairs.map(swapDefaults): _*)).success
      // TODO: This will have to deal with repeatable arguments at some point
    }
  }

}
