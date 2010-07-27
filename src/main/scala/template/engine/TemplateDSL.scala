/*==============================================================================================
| All the classes in this file are used to built the Tempalte DSL. That means
| stuff like: 
|
|  injectContentsOfFile "path/to/file" into "myfile.txt" atPoint "MyPoint"
|
| HOWEVER currently I have to write it like this: 
|
| injectContentsOfFile("src/test/resources/user_inject_Model_Point.txt").into("src/test/resources/model.txt").at("Point")
|
| What do I have to do to be able to drop () and dots?
|
|==============================================================================================*/

package template.engine

import net.liftweb.common.{Box, Empty, Failure, Full}

trait InjectableTrait {

  val dependentTemplate: Template
  val pathToFile: String

  def into(pathToTemplate: String) = InjectableIntoFile(this, pathToTemplate)
}

trait InjectableIntoFileTrait {
  
  val injectable: InjectableTrait
  val pathToTemplate: String
  
  def at(point: String) = TemplateInjection(injectable.pathToFile, pathToTemplate, point, injectable.dependentTemplate)
}


case class Injectable(pathToFile: String, dependentTemplate: Template) extends InjectableTrait

case class InjectableIntoFile(injectable: InjectableTrait, pathToTemplate: String) extends InjectableIntoFileTrait

/**
* This represents something that you want to inject into at file. You are not meant to create instances
* of this class my yourself. Use the Template DSL and instances of this call will get created.
* 
* @param  file              The path to the file whose content you want to inject
* @param  into              The path to the file you want to inject code into
* @param  atPoint           The name of the injection point of the file.
* @param  dependentTemplate The template that wants to inject something into it. 
* 
*/
case class TemplateInjection(file: String, into: String, point: String, dependentTemplate: Template) {
  
  dependentTemplate.addInjection(this)
  
}