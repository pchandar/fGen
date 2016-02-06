package com.pchandar.features

import com.pchandar.utils.TestHelpers
import org.scalatest._

class TimeTokenFeaturesTest extends FlatSpec with ShouldMatchers {


  behavior of "TimeTokenFeaturesTest#addDigitTime"

  it should "return DIGIT_TIME when word contains a digit followed by an am or pm variant" in {
    val doc = TestHelpers.createDocumentWithTokens("5am some word")
    val token = doc.tokens.find(_.string == "5am").get
    TimeTokenFeatures.addDigitTime.f(IndexedSeq(token)) should be (Map(token -> Vector("DIGIT_TIME")))
  }


  it should "return an empty Vector() when word doesn't contain a digit " in {
    val doc = TestHelpers.createDocumentWithTokens("5am some word")
    val token = doc.tokens.find(_.string == "some").get
    TimeTokenFeatures.addDigitTime.f(IndexedSeq(token)) should be (Map(token -> Vector()))
  }

  behavior of "TimeTokenFeaturesTest#addAMPM"


  it should "return an AMPM  when the word is an AM or PM ignoring cases" in {
    val doc = TestHelpers.createDocumentWithTokens("5 am some word")
    val token = doc.tokens.find(_.string == "am").get
    TimeTokenFeatures.addAMPM.f(IndexedSeq(token)) should be (Map(token -> Vector("AMPM")))

    val docB = TestHelpers.createDocumentWithTokens("5 PM some word")
    val tokenB = docB.tokens.find(_.string == "PM").get
    TimeTokenFeatures.addAMPM.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector("AMPM")))
  }


  it should "return an AMPM  when the word is an a.m. or a.m ignoring cases" in {
    val doc = TestHelpers.createDocumentWithTokens("5 a.m. some word")
    val token = doc.tokens.find(_.string == "a.m.").get
    TimeTokenFeatures.addAMPM.f(IndexedSeq(token)) should be (Map(token -> Vector("AMPM")))

    val docB = TestHelpers.createDocumentWithTokens("5 p.m some word")
    val tokenB = docB.tokens.find(_.string == "p.m").get
    TimeTokenFeatures.addAMPM.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector("AMPM")))
  }


  it should " NOT return an AMPM  when the word is not any kind of am, pm, or its variation" in {
    val doc = TestHelpers.createDocumentWithTokens("5 a.m. some word")
    val token = doc.tokens.find(_.string == "some").get
    TimeTokenFeatures.addAMPM.f(IndexedSeq(token)) should be (Map(token -> Vector()))
  }


  behavior of "TimeTokenFeaturesTest#addDigit"


  it should "return a DIGIT when the word is digit" in {
    val doc = TestHelpers.createDocumentWithTokens("5 a.m. some word")
    val token = doc.tokens.find(_.string == "5").get
    TimeTokenFeatures.addDigit.f(IndexedSeq(token)) should be (Map(token -> Vector("DIGIT")))
  }

  it should "NOT return a DIGIT when the word is not a digit" in {
    val doc = TestHelpers.createDocumentWithTokens("5 a.m. some word")
    val token = doc.tokens.find(_.string == "some").get
    TimeTokenFeatures.addDigit.f(IndexedSeq(token)) should be (Map(token -> Vector()))
  }

}
