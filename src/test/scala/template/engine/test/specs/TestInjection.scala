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
  
  val scalteBasic = Scalate(BasicProject, Nil) // don't care about the arguments. 
  
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
    injectContentsOfFile("src/test/resources/user_inject.txt").into("src/test/resources/projectblank.txt").at("Point").inTemplate(BlankProject)  
  }
  
  /**
  *=======
  * The following tests make sure that the correct template are allowed to inject
  * content into templates at the correct time. 
  *======== 
  **/
  
  "When running BasicProject there" should "be 2 injections for projectblank.txt" in {
    val fileName = User.injections(0).into.split("/").last
    val file = FileHelper.loadFile(BlankProject.files.first.file)
    val injections = scalteBasic.injectionsForPointInFile("Point", file)
    injections.size should be === 2
    file.delete
  }
  
  // "B" should "allow C to add something to it's templates" in {
  //   // Because C depends on B it's okay for C to add lines to B. 
  //   val fileName = C.injections(0).into.split("/").last
  //   B.getInjectionsForFile(fileName) should be === List(C.injections(0))
  // }
  
  // "B" should "be allowed to  add something to A" in {
  //     // Because A depends on B it's okay for B to add lines on A
  //     val fileName = B.injections(0).into.split("/").last
  //     A.getInjectionsForFile(fileName) should be === List(B.injections(0))
  //   }
  //   
  //   "D" should "Not be allowed to inject something into any template" in {
  //     // There's not relationsship between D and any other template. 
  //     val fileName = D.injections(0).into.split("/").last
  //     A.getInjectionsForFile(fileName) should be === List(B.injections(0))
  //   }
  //   
  //   // "E" should "Be allowed to add inject something to templates of A" in {
  //   //   // Because E denends on B and B dependens on A
  //   //   val fileName = E.injections(0).into.split("/").last
  //   //   A.getInjectionsForFile(fileName) should be === List(E.injections(0))
  //   // }
  //   
  //   "F" should "Be allowed to inject something to templates of B" in {
  //     // Because F depends on C which Depends on B
  //     val fileName = F.injections(0).into.split("/").last
  //     B.getInjectionsForFile(fileName) should be === List(F.injections(0))
  //   }
  //   
  //   "GroupArguments" should "be allowed for every template to add to every template" in {
  //     // This is used when the user 'freehands' the templates through the console.
  //     val fileName = D.injections(0).into.split("/").last
  //     GroupTemplates(List(D,A)).getInjectionsForFile(fileName).size should be === List(B.injections(0),D.injections(0)).size
  //   }
    
  
}