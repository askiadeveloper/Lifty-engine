package org.lifty.engine 

import java.net.{ URL }
import java.io.{ InputStreamReader }
import net.liftweb.json.{ JsonParser, DefaultFormats }

/*
  This is used to load a description given a URL. 
*/
object DescriptionLoader {
  
  /*
    simply so it knows how to println this stuff for debugging. 
  */
  implicit val formats = DefaultFormats
  
  /*
    This will attempt to load the file at the url and parse it into a Description
  */
  def load(url: URL): Either[Error, Description] = {
    attemptToGetStream(url).fold( 
      err => Left(err),
      inputStream => (for {
        jvalue      <- JsonParser.parseOpt(inputStream)
        description <- jvalue.extractOpt[Description]
      } yield Right(description)).getOrElse(Left(Error("Wasn't able to parse ")))
    )
  }
  
  /*
    This will attempt to create an InputStreamReader from the url passed. 
  */
  private def attemptToGetStream(url: URL): Either[Error, InputStreamReader] = {
    try {
      Right(new InputStreamReader(url.openStream))
    } catch {
      case e: Exception => Left(Error("Wasn't able to read url: %s ".format(e.getMessage)))
    }
  }  
}
