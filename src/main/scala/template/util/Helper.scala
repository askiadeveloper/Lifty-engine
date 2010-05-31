package template.util

object Helper {
  
  def pathOfPackage(thePackage :String) = thePackage.replace("""\.""","/")
  def packageOfPath(path :String) = path.replace("src/main/scala/","").replace("/",".")
  
}