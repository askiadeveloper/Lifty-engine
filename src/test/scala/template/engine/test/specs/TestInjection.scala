package template.engine.test.specs

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.io.File
import template.engine._
import template.util.{FileHelper}
import template.engine.test
import net.liftweb.common.{Box, Empty, Failure, Full}

class TestInjection extends FlatSpec with ShouldMatchers {
  
  // constants
  val BLANK_PROJECT_FILE_PATH = "src/test/resources/projectblank.txt"
  
  val scalateBlank = Scalate(BlankProject, Nil)// don't care about the arguments. 
  val scalateBasic = Scalate(BasicProject, Nil) 
  val scalateModel = Scalate(Model,Nil)
  val scalateUser = Scalate(User,Nil)
  
  object BlankProject extends Template with Create {
    def name = "blank"
    def description = ""
    def files = TemplateFile(BLANK_PROJECT_FILE_PATH,"src/test/output/projectblank.txt") :: Nil
    def arguments = Nil
  }
  
  object BasicProject extends Template with Create {
    def name = "basic"
    def description = ""
    def files = Nil
    def arguments = Nil
    override def dependencies = List(BlankProject, User)
    injectContentsOfFile("src/test/resources/basic_inject.txt").into("src/test/resources/projectblank.txt").at("Point").inTemplate(BlankProject)  
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
    
    injectContentsOfFile("src/test/resources/user_inject.txt").into("src/test/resources/model.txt").at("Point").inTemplate(Model)  
    injectContentsOfFile("src/test/resources/user_inject.txt").into("src/test/resources/model.txt").at("Point2").inTemplate(Model)  
    injectContentsOfFile("src/test/resources/user_inject.txt").into("src/test/resources/projectblank.txt").at("Point").inTemplate(BlankProject)  
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
}