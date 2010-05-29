package template.util

import net.liftweb.common.{Box, Empty, Failure, Full}

object BoxUtil {
	def containsAnyFailures(list :List[Box[_]]): Boolean = {
		!list.forall{ box => !box.isInstanceOf[Failure] }
	}
}