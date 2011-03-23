package org.lifty.engine

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import java.io.{ File }

class TemplateDownloadingTest extends FlatSpec with ShouldMatchers {
  
  val engineInstance = new LiftyEngineTestInstance(Mocks.URI_TO_TEST_DESCRIPTION)

  "Running update-templates" should "download the snippet template" in {
  
    val home = System.getProperty("user.home") 
    val location = home + File.separator + ".lifty"  + File.separator + "snippet.ssp"
  
    // clean up 
    new File(location).delete
    
    // run
    engineInstance.run(List("update-templates")) // don't care about the result. 
    new File(location).exists should be === true
  }
  
  
}

