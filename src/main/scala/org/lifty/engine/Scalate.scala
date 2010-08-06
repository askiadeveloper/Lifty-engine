package org.lifty.engine

import org.fusesource.scalate.{TemplateEngine,DefaultRenderContext}
import org.lifty.util.{TemplateHelper, FileHelper}
import java.io._
import scala.io.{Source}
import java.net.{URL, URISyntaxException}
import scala.util.matching.Regex
import net.liftweb.common._
import org.lifty.engine.commands.{CommandResult}
import org.lifty.util.IOHelper
import org.lifty.util.TemplateHelper._

case class Scalate(template: Template with Create, argumentResults: List[ArgumentResult]) {
    
  val engine = {
    val e = new TemplateEngine
    if (GlobalConfiguration.scalaLibraryPath != "") {
      e.classpath = (GlobalConfiguration.scalaLibraryPath :: 
                          GlobalConfiguration.scalaCompilerPath ::
                          GlobalConfiguration.scalatePath :: Nil).mkString(":")
    }
    e
  }
  
  /**
  * Run scalate on all of the template files the Template specifies
  */
  def run: Box[CommandResult] = { 
    
    // if the template has any dependencies, we need t process those files aswell
    val templateFiles = template.getAllFiles
    
    // Okay i should filter the lists for any files that doesn't end with .ssp and
    // store these files for later. Process each template and then copy the rest
    val toCopy = templateFiles.filter(!_.file.contains(".ssp"))
    val toRender = templateFiles.filter(_.file.contains(".ssp"))
                
    val processedFiles = toRender.map( t => processSingleTemplate(t) ).filter{ _ match {
      case(_,true) => true
      case(_,false) => false
    }}.map{ case(templateFile,true) => templateFile}
    
    val copiedFiles = toCopy.map( t => (t,copy(t.file,TemplateHelper.replaceVariablesInPath(t.destination,argumentResults))) ).filter{ _ match {
      case (_,true) => true
      case (_,false) => false
    }}.map{ case(template,true) => template}
          
    // pretty printing 
    val header = "Running %s with the following arguments:\n".format(template.name)
    val arguments = "\n%s\n".format(argumentResults.map(arg => arg.argument.name+" = "+arg.value).mkString("\n"))
    val files = "\nResulted in the creation of the following files:\n%s"
      .format((processedFiles ::: copiedFiles).map{ path => 
        "  " + TemplateHelper.replaceVariablesInPath(path.destination,argumentResults)
      }.mkString("\n"))
    
    val notice = template.notice(argumentResults) match {
      case Full(notice) => "\n\nNotice:\n%s\n".format(notice)
      case _ => ""
    }
        
    val messageUnusedInjections = if (unusedInjections.size > 0) {
      val stroke = "-----------------------------------------------"
      "\n\nThe template was not able inject the following: \n%s\n%s\n%s".format(
        stroke,
        unusedInjections.map{
        injection => 
          "\n\nCode:\n%s\n\ninto: %s at %s".format(
            processTemplateInMemory(new File(injection.file)),
            injection.into.split("\\.").first,
            injection.point
          )
      }.mkString("\n\n"),
      stroke)
    } else ""
    
    template.postRenderAction(argumentResults)
    template.dependencies.foreach(_.postRenderAction(argumentResults))
  
    cleanScalateCache
        
    Full(CommandResult("%s%s%s%s%s".format(header,arguments,files,notice,messageUnusedInjections)))
  }
  
  /**
  * Prepares this one template file by injecting any code into the template that might be required by 
  * templates that depend on this one. 
  * 
  * @param  file  the file to inject lines into
  */
  def injectLines(file: File): File = {
    val is = new FileInputStream(file)
    val source = Source.fromInputStream(is)
    val regxp = RegularExpressions.INJECTION_POINT
      
    val text: String = (for (line <- source.getLines) yield {
      if(!regxp.findFirstIn(line).isEmpty) {
        val point = regxp.findFirstMatchIn(line).get.group(1)
        injectionsForPointInFile(point, file).map{ injection =>
          val injectionFile = new File(injection.file)
          processTemplateInMemory(injectionFile) + "\n"
        }.mkString("")
      } else line
    }).toList.mkString("")
    
    val newFile = new File(file.getAbsolutePath)
    file.delete
    newFile.createNewFile
    val out = new BufferedWriter(new FileWriter(newFile));
    out.write(text)
    out.close
    newFile 
  }
  
  /**
  * Finds all of the injection points declared in the file 'file'
  * 
  * @param  file  The file to search through
  * @return       A list of the names of each of the injection points
  */
  def injectionPointsInFile(file: File): List[String] = {
    val regxp = RegularExpressions.INJECTION_POINT
    val is = new FileInputStream(file)
    val source = Source.fromInputStream(is)
    (for ( line <- source.getLines if !regxp.findFirstIn(line).isEmpty) yield {
      regxp.findFirstMatchIn(line).get.group(1)
    }).toList
  }
   
  /**
  * Finds all of the injections for the specific injection point 'point' in the file 'file'
  * Only injections that are valid are returned i.e. only injections from files that are about
  * to be created are accepted. 
  * 
  * @param  point The 
  * @param  file  well isn't it obvious
  * @return       dunno
  */
  def injectionsForPointInFile(point: String, file: File) = {
         
    // get all the injections
    val injections = template.injections :::
      template.getAllDependencies.flatMap( _.injections)
        
    // only the ones that have anything to do with this file
    val forFile = injections.filter{ injection =>  
      "_temp_"+injection.into.split("/").last == file.getName
    }
    
    // we only want injections for the current point
    forFile.filter(_.point == point)
  } 
  
  /**
  * This will return a list of all the unused injections. I.e. if one of the dependencies of this template
  * declares an injection that wants to inject something into a file that is not schedueled for creating 
  * then that injections is "unused".
  * 
  * @return A list of unused injections
  */
  def unusedInjections: List[TemplateInjection] = {
    // this should be get all of the injections, then all of the valid injections and return the diff.
    
     // Get all the possible injections
    val injections = template.injections :::
      template.getAllDependencies.flatMap( _.injections)
      
    // all the template files
    val allFiles = (template.files ::: template.getAllDependencies.flatMap(_.files)).map(_.file.split("/").last)
      
    // Get all the valid injections, i.e. injections that have anything to do 
    // with the above files
    val validInjections = injections.filter( injection => allFiles.contains(injection.into))

    // get the ones that are injections, but not valid.
    injections.diff(validInjections)
  }
    
  // The version of Scalate I'm using (1.0 scala 2.7.7) doesn't allow you 
  // change the cache settings. The 2.0 brach does so this can be removed later on
  private def cleanScalateCache: Unit = {
    val scalateBytecodeFolder = new File("bytecode")
    val scalateSourceFolder = new File("source")
    if (scalateSourceFolder.exists) FileHelper.recursiveDelete(scalateSourceFolder)
    if (scalateBytecodeFolder.exists) FileHelper.recursiveDelete(scalateBytecodeFolder)
  }
  
  
  /**
  * This simply processes template and returns the content as a string. If the file has the type
  * .ssp it is rendered with scalate. If not the content is just copied.
  * 
  * @param  file  The path to the file you want to process/copy
  * @return       A string with the copied/rendered content
  */
  private def processTemplateInMemory(file: File): String = {
    val template = FileHelper.loadFile(file.getAbsolutePath)
    try {
      if (template.getAbsolutePath.split("/").last.contains(".ssp")) {
        val sclateTemplate = engine.load(template.getAbsolutePath)
        val buffer = new StringWriter()
        val context = new DefaultRenderContext(new PrintWriter(buffer))
        addArgumentsToContext(context)
        sclateTemplate.render(context)
        buffer.toString.split("\n").dropWhile( _ == "").mkString("\n")
      } else {
        val is = new FileInputStream(template)
        val in = scala.io.Source.fromInputStream(is)
        in.getLines.toList.mkString("")
      }
    } finally {
      template.delete
    }
  }
  
  /**
  * This will process a single scalate template file and save the file in the appropriate 
  * place
  * 
  * @param  templateFile  The template file to process
  * @return               A tuple with the template file and boolean indicating 
  *                       if it suceeded
  */
  private def processSingleTemplate(templateFile: TemplateFile): (TemplateFile,Boolean) = {
    
    val pureTemplateFile = FileHelper.loadFile(templateFile.file)
    val file = injectLines(pureTemplateFile)
    val sclateTemplate = engine.load(file.getAbsolutePath)
    val destinationPath = TemplateHelper.replaceVariablesInPath(templateFile.destination,argumentResults)
    val buffer = new StringWriter()
    val context = new DefaultRenderContext(new PrintWriter(buffer))
    addArgumentsToContext(context)
    sclateTemplate.render(context)
        
    try {
      FileHelper.createFolderStructure(destinationPath)
      val currentPath = new File("").getAbsolutePath // TODO: Not sure this is needed.
      val file = new File(currentPath+"/"+destinationPath)
      if (IOHelper.safeToCreateFile(file)) {
          file.createNewFile
          val out = new BufferedWriter(new FileWriter(file));
          out.write(buffer.toString);
          out.close();
          (templateFile, true)
      } else {
        (templateFile, false)
      }
    } catch {
      case e: Exception => {
        println("exception!")
        println("dest: " + destinationPath)
        e.printStackTrace
        (templateFile, false)
      }
    } finally {
      // Remove the temp file.
      file.delete 
    }
  }
  
  /**
  * this runs through each of the ArgumentResults and adds them to the template context.
  * repeatable arguments gets added as a list
  * 
  * @param  context The context to add the arguments to
  *
  */
  private def addArgumentsToContext(context: DefaultRenderContext): Unit = {
    // recursivly run through the list and add any repeatable argument to the
    // context as a list. 
    def addArgs(arg: ArgumentResult, args: List[ArgumentResult]): Unit = {
      val toAdd = args.filter( _.argument.name == arg.argument.name)
      toAdd match {
        case argument :: rest if rest == Nil => {
          // Add any repeatable arugment with value of "" as an empty list
          argument match {
            case empty if empty.argument.isInstanceOf[Repeatable] && empty.value == "" => 
              context.attributes(empty.argument.name) = List[String]()
            case repeatable if repeatable.argument.isInstanceOf[Repeatable] => 
              context.attributes(repeatable.argument.name) = List(repeatable.value)
            case argument => 
              context.attributes(argument.argument.name) = argument.value
          }
        }
        case argument :: rest => {
          val list = argument :: rest
          if (list.forall(_.argument.isInstanceOf[Repeatable])) { //Add repeatable as list
            context.attributes(arg.argument.name) = list.map(_.value)
          } else {
            context.attributes(arg.argument.name) = list.map(_.value).first
          }
        }
        case Nil => // Empty list
      }
      val rest = (args -- toAdd)
      rest match {
        case argument :: rest => addArgs(argument,(argument::rest))
        case Nil => // done.
      }
    }
    val allArgs = argumentResults:::template.fixedValues
    if (allArgs.size > 0) {
      addArgs(allArgs.first,allArgs)
    }
  }
 
}