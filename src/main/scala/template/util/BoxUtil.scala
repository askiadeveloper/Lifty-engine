package template.util

import net.liftweb.common.{Box, Empty, Failure, Full}

object BoxUtil {
  def containsAnyFailures(list :List[Box[_]]): Boolean = {
    !list.forall{ box => !box.isInstanceOf[Failure] }
  }
  
  def collapseFailures(list: List[Box[_]]): Failure = {
   val failureMsg = list.filter(_.isInstanceOf[Failure]).map( _.asInstanceOf[Failure].msg ).mkString("\n")
   Failure(failureMsg)
  }
  
}