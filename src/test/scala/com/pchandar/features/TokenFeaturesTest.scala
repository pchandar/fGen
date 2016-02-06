package com.pchandar.features

import cc.factorie.app.nlp.pos.PennPosTag
import cc.factorie.app.nlp.{Document, Token}
import com.pchandar.utils.TestHelpers
import org.scalatest.{FlatSpec, Matchers}

class TokenFeaturesTest extends FlatSpec with Matchers{

  behavior of "TokenFeaturesTest#addPunctuationPercentage"

  it should "addPunctuationPercentage" in {

  }

  behavior of "TokenFeaturesTest#addTokenRelativePosition"

  it should "addTokenRelativePosition" in {

  }


  behavior of "TokenFeaturesTest#addContainsPrimeS"

  it should "return PRIME when the word contains an 's" in {
    val token = new Token(new Document("word's"), "word's")
    TokenFeatures.addContainsPrimeS.f(IndexedSeq(token)) should be (Map(token -> Vector("DO=PRIMES")))
  }


  it should "return an empty Vectory when the word does NOT contains an 's" in {
    val token = new Token(new Document("word"), "word")
    TokenFeatures.addContainsPrimeS.f(IndexedSeq(token)) should be (Map(token -> Vector()))
  }


  behavior of "TokenFeaturesTest#addPrefixSuffix"
  it should "return an empty Vector when the length of the word is less than 5" in {
    val token = new Token(new Document("word"), "word")
    TokenFeatures.addPrefixSuffix.f(IndexedSeq(token)) should be (Map(token -> Vector()))
  }

  it should "return the prefix and suffix of a word with length is more than 5 for say SQUIRRELLED " in {
    val token = new Token(new Document("SQUIRRELLED"), "SQUIRRELLED")
    TokenFeatures.addPrefixSuffix.f(IndexedSeq(token)) should be (Map(token -> Vector("P=squi","S=lled")))
  }



  behavior of "TokenFeaturesTest#addSimplifiedPOSTagger"

  it should "return a simplified version of the POSTag (Noun, Adj, PersonalPronoun, ProperNoun) " in {
    val doc = new Document("word")
    val token = new Token(doc, "word")
    token.attr += new PennPosTag(token, 14) //14 --> JJ
    TokenFeatures.addSimplifiedPOSTagger.f(IndexedSeq(token)) should be (Map(token -> Vector("POS=Adj")))


    val tokenB = new Token(doc, "word")
    tokenB.attr += new PennPosTag(token, 20) //20 --> NNP
    TokenFeatures.addSimplifiedPOSTagger.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector("POS=Noun")))
  }

  it should "return a Other 'O' when the tag for the word is O" in {
    val doc = new Document("word")
    val token = new Token(doc, "word")
    token.attr += new PennPosTag(token, 0)
    TokenFeatures.addSimplifiedPOSTagger.f(IndexedSeq(token)) should be (Map(token -> Vector("POS=O")))
  }


  behavior of "TokenFeaturesTest#addSimplifyDigitsWord"
  it should "returns a string with with digits replaced, the whole string with <year> or <num>" in {
    val doc = TestHelpers.createDocumentWithTokens("some words then year 2016 and just num 123")
    val token = doc.tokens.find(_.string == "2016").get
    TokenFeatures.addSimplifyDigitsWord.f(IndexedSeq(token)) should be (Map(token -> Vector("W=<year>")))
    val tokenB = doc.tokens.find(_.string == "123").get
    TokenFeatures.addSimplifyDigitsWord.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector("W=<num>")))
  }


  behavior of "TokenFeaturesTest#addisPunctuation"

  it should "return PUNCTUATION if the word is a punctuation else an empty Vector" in {
    val doc = TestHelpers.createDocumentWithTokens("5 am some word .")
    val token = doc.tokens.find(_.string == ".").get
    TokenFeatures.addisPunctuation.f(IndexedSeq(token)) should be (Map(token -> Vector("PUNCTUATION")))
    val tokenB = doc.tokens.find(_.string == "some").get
    TokenFeatures.addisPunctuation.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector()))

  }

  behavior of "TokenFeaturesTest#addTokenLength"

  it should "return the length of the token with the following  values 0, 1, 2, 3, 4, 5, INTERMIDEATE, BIG" in {
    val doc = TestHelpers.createDocumentWithTokens("5 am some word biggggggg really bigggggggggggggggggggggg")
    val token = doc.tokens.find(_.string == "some").get
    TokenFeatures.addTokenLength.f(IndexedSeq(token)) should be (Map(token -> Vector("TOKEN_LENGTH=4")))

    val tokenB = doc.tokens.find(_.string == "biggggggg").get
    TokenFeatures.addTokenLength.f(IndexedSeq(tokenB)) should be (Map(tokenB -> Vector("TOKEN_LENGTH=INTERMEDIATE")))

    val tokenC = doc.tokens.find(_.string == "bigggggggggggggggggggggg").get
    TokenFeatures.addTokenLength.f(IndexedSeq(tokenC)) should be (Map(tokenC -> Vector("TOKEN_LENGTH=BIG")))
  }

  behavior of "TokenFeaturesTest#addShape"

  it should "return a string that captures the generic 'shape' of the original word" in {
    val doc = TestHelpers.createDocumentWithTokens("5 am some word")
    val token = doc.tokens.find(_.string == "some").get
    TokenFeatures.addShape.f(IndexedSeq(token)) should be (Map(token -> Vector("SHAPE=aa")))
  }

}
