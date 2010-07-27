package template.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io._
import scala.io.{Source}
import template.engine._
import template.util.{FileHelper}
import template.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestInjection extends FlatSpec with ShouldMatchers {
  
  object ModelNameArg extends Argument("modelName")
  object UserNameArg extends Argument("userName")
  
  val userArgumentResults = ArgumentResult(ModelNameArg,"someModelName") :: 
    ArgumentResult(UserNameArg,"someUserName") :: Nil
    
  val scalateBlank = Scalate(BlankProject, Nil)// don't care about the arguments. 
  val scalateBasic = Scalate(BasicProject, userArgumentResults) 
  val scalateModel = Scalate(Model,Nil)
  val scalateUser = Scalate(User,userArgumentResults) 
  
  object BlankProject extends Template with Create {
    def name = "blank"
    def description = ""
    def files = TemplateFile("src/test/resources/projectblank.txt","src/test/output/projectblank.txt") :: Nil
    def arguments = Nil
  }
  
  object BasicProject extends Template with Create {
    def name = "basic"
    def description = ""
    def files = Nil
    def arguments = Nil
    override def dependencies = List(BlankProject, User)
    injectContentsOfFile("src/test/resources/basic_inject_Blank_Point.txt").into("src/test/resources/projectblank.txt").at("Point")
  }
  
  object Model extends Template with Create {
    def name = "model"
    def description = ""
    def files = TemplateFile("src/test/resources/model.txt","src/test/output/model.txt") :: Nil
    def arguments = Nil
  }
  
  object User extends Template with Create {
    def name = "user"
    def description = ""
    def files = Nil
    def arguments = Nil
    override def dependencies = List(Model)

    injectContentsOfFile("src/test/resources/user_inject_Model_Point.ssp") into("src/test/resources/model.txt") at("Point")
    injectContentsOfFile("src/test/resources/user_inject_Model_Point2.ssp").into("src/test/resources/model.txt").at("Point2")
    injectContentsOfFile("src/test/resources/user_inject_Blank_Point.ssp").into("src/test/resources/projectblank.txt").at("Point")
  }
  
  /**
  *=======
  * The following tests make sure that the correct template are allowed to inject
  * content into templates at the correct time. 
  *======== 
  **/
  
  "When running BasicProject there" should "be 2 injections for projectblank.txt" in {
    // testing that injections are allowed by templates that depend on it. 
    // This also shows that it knows how to filter the points.
    val file = FileHelper.loadFile(BlankProject.files.first.file)
    val injections = scalateBasic.injectionsForPointInFile("Point", file)
    injections.size should be === 2
    file.delete
  }
  
  "When running Model there" should "be 0 injections for it's model.txt" in {
    // testing that injections are now accepted if the file that wants to inject
    // something isn't about to be created. 
    val file = FileHelper.loadFile(Model.files.first.file)
    val injections = scalateModel.injectionsForPointInFile("Point", file)
    injections.size should be === 0
    file.delete
  }
  
  "When running User there" should "be 1 injections for Model's model.txt" in {
    // testing that injections are allowed by templates that depend on it. 
    val file = FileHelper.loadFile(Model.files.first.file)
    val injections = scalateUser.injectionsForPointInFile("Point", file)
    injections.size should be === 1
    file.delete
  }
  
  "When running a group of templates with User and ProjectBlank there" should "be 1 injection for projectblank.txt" in {
    // Testing that a GroupTemplate allows all of the templates to inject code to each of the other templates 
    // that are being created. 
    val grp = GroupTemplates(List(BlankProject,User))
    val file = FileHelper.loadFile(BlankProject.files.first.file)
    val grpScalate = Scalate(grp,Nil);
    val injections = grpScalate.injectionsForPointInFile("Point", file)
    injections.size should be === 1
    file.delete
  } 
  
  /**
  *=======
  * The following tests make sure that the the framework is able to point the correct 
  * injection points in the different templates
  *======== 
  **/

  "The model template" should "have 2 injection points: Point,Point2" in {
    val file = FileHelper.loadFile(Model.files.first.file)
    scalateModel.injectionPointsInFile(file) should be === List("Point","Point2")
    file.delete
  }
  
  /**
  *=======
  * The following tests make sure that the the right content is injected
  * into the right files at the right places
  *======== 
  **/
  
  "When running User something" should "get injected into the model template" in {
    val file = FileHelper.loadFile(Model.files.first.file)
    
    val result = scalateUser.injectLines(file)
    val resultIs = new FileInputStream(result)
    val resultS = Source.fromInputStream(resultIs)
    
    val wantedResult = new File("src/test/resources/correct_results/_temp_model_running_user.txt")
    val wantedResultIs = new FileInputStream(wantedResult)
    val wantedResultS = Source.fromInputStream(wantedResultIs)
    
    resultS.getLines.toList should be === wantedResultS.getLines.toList
    
    file.delete
  }
  
  "When running Model there" should "be no sign of the injection points" in {
    val file = FileHelper.loadFile(Model.files.first.file)

    val result = scalateModel.injectLines(file)
    val resultIs = new FileInputStream(result)
    val resultS = Source.fromInputStream(resultIs)
    
    val wantedResult = new File("src/test/resources/correct_results/_temp_model_running_model.txt")
    val wantedResultIs = new FileInputStream(wantedResult)
    val wantedResultS = Source.fromInputStream(wantedResultIs)
    
    resultS.getLines.toList should be === wantedResultS.getLines.toList
    
    file.delete
  }
  
}