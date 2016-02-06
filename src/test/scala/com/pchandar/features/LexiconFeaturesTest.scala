package com.pchandar.features

import cc.factorie.app.nlp.Token
import cc.factorie.variable.{BinaryFeatureVectorVariable, CategoricalVectorDomain}
import com.pchandar.utils.TestHelpers
import org.scalatest.{FlatSpec, ShouldMatchers}

class LexiconFeaturesTest extends FlatSpec with ShouldMatchers {

  behavior of "LexiconFeaturesTest"

  val lexiconFeatures = new LexiconFeatures(TestHelpers.nlpResources)
  it should "addWikipedia" in {
    val doc = TestHelpers.createDocumentWithTokens("John met him in Abu Dhabi")

    val out = getFeatures(doc.tokens.toIndexedSeq, FactorieFeature("x", lexiconFeatures.addWikipedia.f))

    out.find(_._1.string == "Dhabi").get._2 should be(
      Seq("WIKI-LOCATION","WIKI-LOCATION-REDIRECT"))
  }

  it should "addIELS" in {
    val doc = TestHelpers.createDocumentWithTokens("John met him in Philadelphia")

    val out = getFeatures(doc.tokens.toIndexedSeq, FactorieFeature("x", lexiconFeatures.addIELS.f))

    out.find(_._1.string == "Philadelphia").get._2 should be(Seq("CITY"))

  }


  private def getFeatures(tokens: IndexedSeq[Token], factorieFeature: FactorieFeature): Map[Token, Seq[String]] = {
    object TestFactorieFeatureDomain extends CategoricalVectorDomain[String]
    class TestFactorieFeature(val token: Token) extends BinaryFeatureVectorVariable[String] {
      def domain = TestFactorieFeatureDomain
      override def skipNonCategories = true
    }
    tokens.foreach(t => t.attr += new TestFactorieFeature(t))
    factorieFeature.f(tokens.toIndexedSeq, (t: Token) => t.attr[TestFactorieFeature])
    tokens.map(t => (t, t.attr[TestFactorieFeature].activeCategories))(scala.collection.breakOut)
  }

}
