package template.engine

import sbt._

case class SBTLogger(logger: sbt.Logger) {
  
  def error (message: => java.lang.String) = logger.error(message)
  def info (message: => java.lang.String) = logger.info(message)
  def warn (message: => java.lang.String) = logger.warn(message)
  def success (message : => java.lang.String) = logger.success(message)
  
  
}

object TemplateEngineLogger {
  
  
  def error (message: => java.lang.String) = println("[error] " + message)
  def info (message: => java.lang.String) = println("[info] " + message)
  def warn (message: => java.lang.String) = println("[warn] " + message)
  def success (message : => java.lang.String) = println("[sucess] " + message)
  
  
}

