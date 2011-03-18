package org.lifty.engine

import java.io.File
import org.lifty.engine._
import org.lifty.engine.InputParser._

object Main {
  def main(args: Array[String]): Unit = {
    
    val url = new File("src/test/resources/test-descriptor.json").toURI.toURL
    
    val output = for ( 
      description <- DescriptionLoader.load(url).right ; 
      command     <- parseCommand(description, args.toList).right
    ) yield command match {
      case (TemplatesCommand, _)  => "you invoked the templates command"
      case (HelpCommand,_)        => "you invoked the help command"
      case (CreateCommand, rest)  => for ( template <- parseTemplate(description, rest).right ;
                                           env      <- parseArguments(description, template._1, template._2).right )
                                     yield env.toString
    }
    
    output match {
      case Left(Error(msg)) => println(msg)
      case Right(r) => println(r.toString)
    }
    
  }
}
