package template.util

import template.engine.{ArgumentResult}

object Helper {
  
  def pathOfPackage(thePackage :String) = thePackage.replace(".","/")
  def packageOfPath(path :String) = path.replace("src/main/scala/","").replace("/",".")
 
	// looks through the path string for any variables (i.e ${someVal}) and replaces
	// it with the acctual value passed to the operation
	def replaceVariablesInPath(path: String, arguments: List[ArgumentResult]): String = {
		
		def findAndTransformValueForArgument(name: String): String = {
			try {
				arguments.filter( _.argument.name == name ).first.pathValue
			} catch {
				case e: Exception => println(e);""
			}
		}
		
		var newPath = path
		"""\$\{\w*\}""".r.findAllIn(path).toList match {
			case list if !list.isEmpty => {
				list.map(_.toString).foreach{ variable => 
					val argName = variable.replace("${","").replace("}","") //TODO: make prettier?
					newPath = newPath.replace(variable,findAndTransformValueForArgument(argName))
				}
				newPath
			}
			case _ => path
		}
	}
	
}