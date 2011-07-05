//
//  Storage.scala
//  Lifty engine
//
//  Created by Mads Hartmann Jensen on 2011-06-16.

package org.lifty.engine.io

import scalaz._
import scalaz.effects._
import Scalaz._
import java.net.{ URL }
import java.io.{ File }
import org.lifty.engine.{ Error, Description }
import Downloader.{ download }
import DescriptionLoader.{ load }

case class Recipe(descriptor: File, templates: Seq[File])

/*
  Storage component. It's used to download, store and fetch the local versions of the
  different recipes. The structure of the storage is as follows:

  <pre>
  ~/.lifty
    recipe name
      recipe name.json
      template1.ssp
      template2.ssp
  </pre>
*/
trait Storage {

  private val / = File.separator

  val root: File

  // TODO: Create it if it doesn't exist
  lazy val storage = file(root.getAbsolutePath + / + ".lifty")

  // attempts to fetch a recipe from the storage
  def recipe(name: String): IO[Validation[Error, Recipe]] = io {
    (for {
      folder     <- storage.listFiles.filter( f => f.isDirectory && f.getName == name ).headOption
      descriptor <- folder.listFiles.filter( f => f.isFile && f.getName == name+".json").headOption
    } yield {
      val templates = folder.listFiles.filter( f => f.isFile && f.getName.split("\\.").lastOption.getOrElse("") == "ssp")
      Recipe(descriptor, templates).success
    }).getOrElse(Error("No recipe named %s in the storage.".format(name)).fail)
  }

  // Attempts to store a recipe at the given url in the storage under the given name.
  def storeRecipe(name: String, url: URL): IO[Validation[Error, Recipe]] = {
    
    val recipe = file(List(storage.getAbsolutePath, name, name+".json").mkString(/))

    download(url, recipe).map( _.fold( err => err.fail,
      file => load(file).unsafePerformIO.fold( err => err.fail,
        description => Recipe(file, storeSourcesOfDescription(name, description)).success
      )
    )) 
  }
  
  // Returns a list with all of the recipes currently in the storage. 
  def allRecipes: IO[List[Recipe]] = io {
    storage.listFiles
           .filter( _.isDirectory)          
           .map( _.getName)
           .map( recipe(_).unsafePerformIO )
           .filter( _.isSuccess )
           .map( _.toOption.get )
           .toList
  }
  
  // Deletes a recipe from the store
  def deleteRecipe(name: String): IO[String] = io {
    storage.listFiles
           .filter( f => f.isDirectory && f.getName == name )
           .headOption
           .foreach( recursiveDelete )
    "Removed %s from the storage".format(name)
  }
  
  private def recursiveDelete(file: File): Unit = {
    if (file.isDirectory && !file.listFiles.isEmpty) {
      file.listFiles.foreach( recursiveDelete ) 
      file.delete
    } else {
      file.delete
    }
  }
  
  protected def file(path: String) = new File(path)
  
  // TODO: shoud return an option. 
  private def storeSourcesOfDescription(recipeName: String, description: Description): List[File] = {     
    description.sources.map { source => 
      download(new URL(source.url), file(List(storage.getAbsolutePath, recipeName, source.name).mkString(/)))
        .unsafePerformIO
        .toOption
        .get
    }
  }
}

object HomeStorage extends Storage {

  val root = file(System.getProperty("user.home"))

}

// object TestStorage extends Storage {
//
// }

/*
  scala> import org.lifty.engine.io.HomeStorage
  import org.lifty.engine.io.HomeStorage

  scala> HomeStorage.deleteRecipe("test")      
  res0: scalaz.effects.IO[String] = scalaz.effects.IO$$anon$2@1832bab5

  scala> res0.unsafePerformIO                  
  res1: String = Removed test from the storage

  scala> HomeStorage.storeRecipe("test", new java.net.URL("https://raw.github.com/Lifty/Lifty-engine/xlifty/lifty-engine/src/test/resources/test-descriptor.json"))
  res2: scalaz.effects.IO[scalaz.Validation[org.lifty.engine.Error,org.lifty.engine.io.Recipe]] = scalaz.effects.IO$$anon$2@49119a3a

  scala> res2.unsafePerformIO
  res3: scalaz.Validation[org.lifty.engine.Error,org.lifty.engine.io.Recipe] = Success(Recipe(/Users/Mads/.lifty/test/test.json,List(/Users/Mads/.lifty/test/snippet)))

*/