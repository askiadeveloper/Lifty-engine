import sbt._

class SimpleBuildToolTemplateEngine(info: ProjectInfo) extends ParentProject(info) {  
  
  lazy val scalate_fork = project("scalate-fork", "scalate-fork", new ScalateFork(_))
  lazy val lifty_engine = project("lifty-engine", "lifty-engine", new LiftyEngine(_), scalate_fork)
  
  class ScalateFork(info: ProjectInfo) extends DefaultProject(info) {  
    /*
      As long as SBT isn't able to use Scala 2.8.x this project is needed. It's a patched
      version of Scalate that compiles under Scala 2.7.7. 
    */
    override def version = OpaqueVersion("1.1-PATCHED")
    
    lazy val maven  = MavenRepository("Maven Central Repo","http://repo1.maven.org/maven2/")
    lazy val javam2 = MavenRepository("java.net Maven 2 Repo", "http://download.java.net/maven/2")
    
    override def libraryDependencies = Set(
      "javax.servlet" % "servlet-api" % "2.5" % "compile->default",
      "com.sun.jersey" % "jersey-server" % "1.1.5" % "compile->default",
      "opensymphony" % "sitemesh" % "2.3" % "compile->default",
      "log4j" % "log4j" % "1.2.14" % "compile->default",
      "org.scalatest" % "scalatest" % "1.0" % "test",
      "junit" % "junit" % "4.5" % "test") 
    
    override def unmanagedClasspath = super.unmanagedClasspath +++ 
      (Path.fromFile(buildScalaInstance.compilerJar)) +++ 
      (Path.fromFile(buildScalaInstance.libraryJar.getPath))
    
    override def managedStyle = ManagedStyle.Maven
    val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
    Credentials(Path.userHome / "dev" / ".nexus_credentials", log)
  }

  class LiftyEngine(info: ProjectInfo) extends ProcessorProject(info) { 
    
    override def version = OpaqueVersion("0.6")
        
    override def libraryDependencies = Set(
      "net.liftweb" % "lift-common" % "2.0-M5" % "compile->default",
      "org.scalatest" % "scalatest" % "1.0" % "test") 

    override def unmanagedClasspath = super.unmanagedClasspath +++ 
      (Path.fromFile(buildScalaInstance.compilerJar)) +++ 
      (Path.fromFile(buildScalaInstance.libraryJar.getPath))

    override def compileOptions = super.compileOptions ++
        Seq("-unchecked","-encoding", "utf8").map(x => CompileOption(x))

    override def managedStyle = ManagedStyle.Maven
    val publishTo = "Scala Tools Nexus" at "http://nexus.scala-tools.org/content/repositories/releases/"
    Credentials(Path.userHome / "dev" / ".nexus_credentials", log)
  }
}