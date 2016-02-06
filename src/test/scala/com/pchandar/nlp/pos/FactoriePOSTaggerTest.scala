package com.pchandar.nlp.pos

import com.pchandar.utils.TestHelpers
import org.scalatest._

class FactoriePOSTaggerTest extends FlatSpec with ShouldMatchers {

  behavior of classOf[FactoriePOSTagger].getSimpleName
  val factorieTagger = new FactoriePOSTagger(TestHelpers.nlpResources.posTagger)

  //TODO: Enable this test when GIT LFS is setup
  ignore should "be able to part of speech tag documents" in {
    val doc = TestHelpers.createDocumentWithTokens("This is a sample test")
    factorieTagger.process(doc)
    val postTags = doc.tokens.map(t => t.posTag.categoryValue)
    postTags should be (List("DT","VBZ","DT","NN","NN"))
  }

}
