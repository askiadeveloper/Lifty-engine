/*==============================================================================================
| All the classes in this file are used to built the Tempalte DSL. That means
| stuff like: 
|
|  injectContentsOfFile "path/to/file" into "myfile.txt" atPoint "MyPoint" inTemplate "empty"
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
  
  def at(point: String) = InjectableIntoFileWithPoint(this, point) 
}

trait InjectableIntoFileWithPointTrait {
  
  val injectableIntoFile: InjectableIntoFileTrait
  val point: String
  
  def inTemplate(template: Template) = 
    TemplateInjection(
      injectableIntoFile.injectable.pathToFile, 
      injectableIntoFile.pathToTemplate, 
      point, 
      template, 
      injectableIntoFile.injectable.dependentTemplate)
}

case class Injectable(pathToFile: String, dependentTemplate: Template) extends InjectableTrait

case class InjectableIntoFile(injectable: InjectableTrait, pathToTemplate: String) extends InjectableIntoFileTrait

case class InjectableIntoFileWithPoint(injectableIntoFile: InjectableIntoFileTrait, point: String) extends InjectableIntoFileWithPointTrait

/**
* This represents something that you want to inject into at file. You are not meant to create instances
* of this class my yourself. Use the Template DSL and instances of this call will get created.
* 
* @param  file              The path to the file whose content you want to inject
* @param  into              The path to the file you want to inject code into
* @param  atPoint           The name of the injection point of the file.
* @param  template          The tempalte whose file you want to inject somethign into
* @param  dependentTemplate The template that wants to inject something into it. 
* 
*/
case class TemplateInjection(file: String, into: String, atPoint: String, template: Template, dependentTemplate: Template) {
  
  dependentTemplate.addInjection(this)
  
}