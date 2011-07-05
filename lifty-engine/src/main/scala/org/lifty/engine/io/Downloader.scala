// 
//  Downloader.scala
//  Lifty engine 
//  
//  Created by Mads Hartmann Jensen on 2011-06-16.

package org.lifty.engine.io

import scalaz._
import scalaz.effects._
import Scalaz._
import java.net.{ URL }
import java.io.{ File, BufferedReader, InputStreamReader, FileWriter }
import org.lifty.engine.{ Error }

object Downloader {
  
  // Downloads a file from the interwebs and stores it locally. Returns a 
  // reference to the new file if successfull
  def download(from: URL, to: File): IO[Validation[Error,File]] = io {
    // eeeewwww, java api 
    try {
      val in = new BufferedReader(new InputStreamReader(from.openStream))
      var line = in.readLine()
      var text = new StringBuffer("")

      while (line != null) {
        text.append(line)
        line = in.readLine()
      }

      to.getParentFile.mkdirs()
      to.createNewFile()

      val writer = new FileWriter(to)
      writer.write(text.toString)
      writer.flush()
      writer.close()
      to.success
    } catch {
      case e: Exception => Error("Failure in Downloader.download").fail
    }
  }
  
}