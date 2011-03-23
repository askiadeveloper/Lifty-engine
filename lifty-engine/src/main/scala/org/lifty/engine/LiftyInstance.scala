package org.lifty.engine

import java.net.{ URL, URI }
import scalaz._
import Scalaz._

/*
  A LiftyEngineComponent consists of three parts

  - A URL to a description.json file that describes the concrete LiftyEngine and
    the templates that it can crete.
  - A compotent to parse the strings inputtet from the user. rules for parsing the
    strings are always they same. It has been implemented as a trait with the self-type
    of LiftyEngineComponent because it's easier to organize the code that way for me.
  - A component to request input from the user when needed. This is a Type varialbe as
    I need two concrete implementations of this. One when running automated tests and
    one when I need real input from the user. See LiftyEngineTestInstance and
    LiftyEngineInstance

*/
trait LiftyEngineComponent extends InputParser {

  // A type that covers instance that can request input from
  // the user
  type InputComponent = {
    def requestInput(msg: String): String
  }

  // The component to use when requesting input from the user
  val inputComponent: InputComponent

  // The URL where the json description is stored
  val descriptionUrl: URL

  // the result of parsing the JSON description file.
  val description = DescriptionLoader.load(descriptionUrl).fold(
    err => throw new Exception(err.message), //TODO: Do I really want to throw an exception?
    description => description)

  // This main method of interest. This takes a list of arguments and
  // executes the appropriate actions - if the input is valid it will
  // return a string describing what it created - otherwise an error
  // to display to the user.
  def run(args: Seq[String]): Validation[Error, String] = {
    (for {
      parseCmdResult <- parseCommand(args.toList)
      (command, rest) = parseCmdResult
      result <- runCommand(command, rest)
    } yield result)
  }

  // Given a Command and a list og arguments this will run the actions
  // associated with the Command and return the result as a String.
  private def runCommand(command: Command, args: List[String]): Validation[Error, String] = {
    command match {
      case TemplatesCommand =>
        (for {
          template <- description.templates
          output = "%s\n%s".format(template.name, template.arguments.map("  " + _.name).mkString("\n"))
        } yield output).mkString("\n").success

      case HelpCommand =>
        "you invoked the help command".success

      case CreateCommand =>
        for {
          templateResult <- parseTemplate(args)
          (template, rest) = templateResult
          env <- parseArguments(template, rest)
        } yield env.toString

      case UpdateTemplatesCommand => 
        (description.templates.flatMap { _.files } map { file => 
          (file.file, description.repository +"/"+ file.file) // '/' is okay. It's a URI
        } map { case (filePath, uri) => 
          TemplateDownloader.downloadTemplate(new URI(uri),filePath) // SIDEEFFECT
          "Downloaded template: %s".format(uri)
        }).mkString("\n").success

      case _ =>
        Error("Command doesn't exist").fail
    }
  }
}

// A specific configuration of LiftyEngineComponent which is used
// for tests.
class LiftyEngineTestInstance(val descriptionUrl: URL) extends LiftyEngineComponent {

  val inputComponent = InputEmulatorComponent

}

// A specific configuration of LiftyEngineComponent which is used
// when running the 'real' application.
class LiftyEngineInstance(val descriptionUrl: URL) extends LiftyEngineComponent {

  val inputComponent = JLineInputComponent

}

