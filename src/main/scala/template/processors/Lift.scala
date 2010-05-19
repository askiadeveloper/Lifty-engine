package template.processors

import template.engine._

trait DefaultLiftTemplate extends Template with Create with Delete{}

object SnippetTemplate extends DefaultLiftTemplate {
	
	override def name = "snippet"
	override def arguments = Nil
	
}