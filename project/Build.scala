import sbt._
import Keys._

object LiftyBuild extends Build {

  lazy val projects = Seq(engine, plugin)
  
  lazy val engine = Project("lifty-engine", file("lifty-engine"))
  
  lazy val plugin = Project("lifty-plugin", file("lifty-plugin")) dependsOn(engine)
    
}

