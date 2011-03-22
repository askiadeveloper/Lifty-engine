package org.lifty.engine

import java.net.{ URL }
import java.io.{ InputStreamReader }
import net.liftweb.json.{ JsonParser, DefaultFormats }

import scalaz._
import Scalaz._

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
  def load(url: URL): Validation[Error, Description] = {
    attemptToGetStream(url).fold(
      err => err.fail,
      inputStream => (for {
        jvalue      <- JsonParser.parseOpt(inputStream)
        description <- jvalue.extractOpt[Description]
      } yield description.success).getOrElse(Error("Wasn't able to parse ").fail)
    )
  }

  /*
    This will attempt to create an InputStreamReader from the url passed.
  */
  private def attemptToGetStream(url: URL): Validation[Error, InputStreamReader] = {
    try {
      new InputStreamReader(url.openStream).success
    } catch {
      case e: Exception => Error("Wasn't able to read url: %s ".format(e.getMessage)).fail
    }
  }
}
