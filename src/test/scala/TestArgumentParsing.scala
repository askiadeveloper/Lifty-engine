import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestArgumentParsing extends FlatSpec with ShouldMatchers {
		
		/* fixtures
		--------------------------------------------- */
		val inputForArg1 = "val1"
		val inputForArg2 = "val2"
		val defaultForArg2 = "default"
		
		object TestTemplate extends Template with Create {
			def name = "TestTemplate"
			def arguments = {
				object normalArgument extends Argument("arg1")
				object defaultArgument extends Argument("arg2") with Default with Value{ value = defaultForArg2}
				normalArgument :: defaultArgument :: Nil
			}
			def files = Nil
		}
		

		/* let the testing begin
		--------------------------------------------- */
			
		"Argument" should "be required" in {
			val input = Nil
			val argumentResults = TestTemplate.parseArguments(input)
			val errorMsg = argumentResults.asInstanceOf[Failure].msg
			
			argumentResults.isInstanceOf[Failure] should be === true
			errorMsg should be === "[error] The argument 'arg1' is required"
		}
		
		it should "accept a value" in  {
			val input = List("arg1=%s".format(inputForArg1))
			val argumentResults = TestTemplate.parseArguments(input)
			argumentResults.open_!.first should be === ArgumentResult(TestTemplate.arguments.first,inputForArg1)
		}
		
		"Superfluous arguments" should "be ignored" in {
			val input = List("arg1=%s".format(inputForArg1),"test2=hejsa2","test3=hejsa3")
			val argumentResults = TestTemplate.parseArguments(input)
			argumentResults.open_! should be === List(
				ArgumentResult(TestTemplate.arguments(0),inputForArg1),
				ArgumentResult(TestTemplate.arguments(1),defaultForArg2)
			)
		}
		
		"Default argument" should "still accept a vaule" in {
			val input = List("arg1=%s"format(inputForArg1),"arg2=%s".format(inputForArg2))
			val argumentResults = TestTemplate.parseArguments(input)
			argumentResults should be === Full(List(
				ArgumentResult(TestTemplate.arguments(0),inputForArg1),
				ArgumentResult(TestTemplate.arguments(1),inputForArg2)
			))		
		}
		
		it should "fallback to the default value" in {
			val input = List("arg1=%s".format(inputForArg1))
			val argumentResults = TestTemplate.parseArguments(input)
			argumentResults should be === Full(List(
				ArgumentResult(TestTemplate.arguments(0),inputForArg1),
				ArgumentResult(TestTemplate.arguments(1),defaultForArg2)
			))
		}
	
	
	
}