package org.lifty.engine

import org.lifty.engine.io._
import java.net.{ URL, URI }
import scalaz._
import scalaz.effects._
import Scalaz._

trait Lifty extends InputParser {

  // A type that covers instance that can request input from the user
  type InputComponent = {
    def requestInput(msg: String): IO[String]
  }

  // The component to use when requesting input from the user
  val inputComponent: InputComponent
  
  // The storage component. Used to store/read recipes 
  val storageComponent: Storage

  // This main method of interest. This takes a list of arguments and
  // executes the appropriate actions - if the input is valid it will
  // return a string describing what it created - otherwise an error
  // to display to the user.
  def run(args: List[String]): Validation[Error, String] = {        
    (for {
      parseCmdResult  <- parseCommand(args.toList).toOption
      (command, rest) = parseCmdResult
    } yield runCommand(command, rest)) getOrElse( Error("No such command").fail )
  }

  // Given a Command and a list og arguments this will run the actions
  // associated with the Command and return the result as a String.
  private def runCommand(command: Command, args: List[String]): Validation[Error, String] = {    
    command match {
      
      case RecipesCommand => {
        storageComponent.allRecipes.unsafePerformIO.map(_.toString).mkString("\n").success
      }

      case LearnCommand => {
        "Not yet supported".success
      }
      
      case TemplatesCommand => {
        args.headOption.map { name => 
          descriptionOfRecipe(name).flatMap { description => 
            (for {
              template <- description.templates
              output = "%s\n%s".format(template.name, template.arguments.map("  " + _.name).mkString("\n"))
            } yield output).mkString("\n").success
          }
        } getOrElse( Error("You have to supply the name of the recipe").fail ) 
      }

      case HelpCommand =>
        "you invoked the help command".success

      case CreateCommand => {
        args.headOption.map { name => 
          descriptionOfRecipe(name).flatMap { description => 
            parseTemplate(description, args).flatMap { tuple => 
              val (template,rest) = tuple
              parseArguments(template, rest).flatMap { env => 
                env.toString.success
              }
            }
          }
        } getOrElse( Error("You have to supply the name of the recipe").fail )
      }
        
      // TODO: Re-implement this so it compiles again
      // case UpdateTemplatesCommand => 
      //   (description.templates.flatMap { _.files } map { file => 
      //     (file.file, description.repository +"/"+ file.file) // '/' is okay. It's a URI
      //   } map { case (filePath, uri) => 
      //     TemplateDownloader.downloadTemplate(new URI(uri),filePath) // SIDEEFFECT
      //     "Downloaded template: %s".format(uri)
      //   }).mkString("\n").success

      case _ =>
        Error("Command doesn't exist").fail
    }
  }
  
  private def descriptionOfRecipe(recipeName: String): Validation[Error,Description] = {
    // TODO: Performs IO 
    storageComponent.recipe(recipeName).unsafePerformIO.flatMap { recipe => 
      DescriptionLoader.load(recipe.descriptor).unsafePerformIO
    } 
  }
}

// A specific configuration of Lifty which is used for tests.
object LiftyTestInstance extends Lifty {

  val inputComponent = EmulatedInputReaderComponent
  val storageComponent = HomeStorage

}

// A specific configuration of Lifty which is used when running the 'real' 
// application.
object LiftyInstance extends Lifty {

  val inputComponent = InputReaderComponent
  val storageComponent = HomeStorage

}

