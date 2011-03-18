package org.lifty.engine

import org.lifty.engine.IO._

object InputParser {
  
  /*
    Given a list of arguments it will (if successful) return the appropriate Command 
    and the rest of the arguments. Otherwise an instance of Error.
  */
  def parseCommand(description: Description, arguments: List[String]): Either[Error, (Command,List[String])] = {
    if (arguments.nonEmpty) {
      arguments.head match {
        case TemplatesCommand.keyword => Right(TemplatesCommand, arguments.tail)
        case HelpCommand.keyword      => Right(HelpCommand, arguments.tail)
        case CreateCommand.keyword    => Right(CreateCommand, arguments.tail)
        case command                  => Left(Error("No command named %s".format(command)))
      }
    } else { Left(Error("You have to supply an argument")) }
  }
  
  /*
    Given a list of arguments it will (if successful) return the appropriate Template
    and the rest of the arguments. Otherwise an instance of Error 
  */
  def parseTemplate(description: Description, arguments: List[String]): Either[Error, (Template,List[String])] = {
    if (arguments.nonEmpty) {      
      (for { 
        template <- description.templates.filter( _.name == arguments.head ).headOption 
      } yield Right((template, arguments.tail))).getOrElse(Left(Error("No template with the name %s".format(arguments.head))))
    } else { Left(Error("You have to write the name of the template"))}
  }
	
	/*
    Given a Description, a Template and a list of arguments it will create an Environment. 
	*/
  def parseArguments(description: Description, template: Template, arguments: List[String]): Either[Error, Environment] = {
      
      // request input for an argument that is missing
      val requestInputForMissingArgument = ( argument: Argument ) => {
        (argument.name, requestInputForArgument(argument)) // TODO: This is a side-effect. IO-Monad? 
      }
      
      // map arguments to a string and swap defaults. Request input if no default exists
      val swapDefaults = ( tuple: (Argument, String) ) => {
        val (argument,value) = tuple
        if (value == "_") {
          requestInputForMissingArgument(argument) 
        } else (argument.name, value)
      }
      
      // filter optional arguments
      val filterOptional = ( argument: Argument ) => {
        !argument.optional.isEmpty && argument.optional.get == true
      }
      
      // pair the values passed with the arguments needed 
      val pairs = template.arguments.zip(arguments)
      
      if (pairs.size == template.arguments.size) {                  // correct number of arguments
        Right(Environment(template, Map( pairs.map(swapDefaults):_*) ))
      } else if (pairs.size < template.arguments.size) {            // too few arguments. Request input
        val kvs = pairs.map( swapDefaults ) ::: 
                  template.arguments
                          .slice(pairs.size,template.arguments.size) // get the missing arguments
                          .filter( filterOptional )                  // filter the ones that are optional
                          .map( requestInputForMissingArgument )     // request input for the ones that are missing
        Right(Environment(template, Map( kvs:_* )
        ))
      } else {                                                       // Too many. Deal with repeatable arguments
        Right(Environment(template, Map( pairs.map(swapDefaults):_* )))
        // TODO: This will have to deal with repeatable arguments at some point
      }
  }
  
}
