package com.pchandar.features

import cc.factorie.app.nlp.Token
import cc.factorie.app.nlp.lemma.WordNetLemmatizer
import cc.factorie.app.nlp.pos.PennPosTag
import cc.factorie.app.strings._

import scala.util.matching.Regex

object TokenFeatures {

  val addSimplifyDigitsWord = SentenceFeature("TokenFeatures.addSimplifyDigitsWord",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val rawWord = token.string
      val word = simplifyDigits(rawWord).toLowerCase
      Vector(s"W=$word")
    })

  /** Return a string that captures the generic "shape" of the original word,
    * mapping lowercase alphabetics to 'a', uppercase to 'A', digits to '1', whitespace to ' '.
    * Skip more than 'maxRepetitions' of the same character class. */
  val addShape = SentenceFeature("TokenFeatures.addShape",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val rawWord = token.string
      Vector(s"SHAPE=${cc.factorie.app.strings.stringShape(rawWord, 2)}")
    })

  val addisPunctuation = SentenceFeature("TokenFeatures.addisPunctuation",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      if (token.isPunctuation) Vector("PUNCTUATION")
      else Vector()

    })

  val addPrefixSuffix = SentenceFeature("TokenFeatures.addPrefixSuffix",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val word = simplifyDigits(token.string).toLowerCase
      if (word.length > 5)
        Vector("P=" + cc.factorie.app.strings.prefix(word, 4), "S=" + cc.factorie.app.strings.suffix(word, 4))
      else
        Vector()
    })

  val addWordNetWord = SentenceFeature("TokenFeatures.addWordNetWord",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val rawWord = token.string
      val word = simplifyDigits(rawWord).toLowerCase
      val pennPosTag = token.attr[PennPosTag]
      val wordNetWord = WordNetLemmatizer.lemma(word, pennPosTag.categoryValue)
      Vector(s"W=$wordNetWord")

    })

  val addContainsPrimeS = SentenceFeature("TokenFeatures.addContainsPrimeS",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val rawWord = token.string
      if (rawWord.contains("'s")) Vector("DO=PRIMES")
      else Vector()

    })

  val addSimplifiedPOSTagger = SentenceFeature("TokenFeatures.addSimplifiedPOSTagger",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val pennPosTag = token.attr[PennPosTag]
      val postag =
        if (pennPosTag.isNoun) "Noun"
        else if (pennPosTag.isAdjective) "Adj"
        else if (pennPosTag.isVerb) "Verb"
        else if (pennPosTag.isPersonalPronoun) "PersonalPronoun"
        else if (pennPosTag.isProperNoun) "ProperNoun"
        else "O"

      if (postag != "O") Vector(s"POS=$postag")
      else Vector(s"POS=O")
    })


  val addPunctuationPercentage = SentenceFeature("TokenFeatures.addPunctuationPercentage",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      def getPunctPercentage(line: String):Double = {
        val punctRegex =  new Regex("\\p{Punct}+")
        val spaceChars = " ".r.findAllIn(line).length
        val punctCount = punctRegex.findAllIn(line).length - spaceChars
        val punctPercentage: Double = (punctCount.toDouble/line.length) * 100
        punctPercentage
      }
      token.string.trim match {
        case x if x.length == 0 =>
          Vector(s"NEWLINE")
        case _ =>
          val punctPercentage = getPunctPercentage(token.string)
          if (punctPercentage > 95)
            Vector(s"PUNCTUATION")
          else
            Vector()
      }

    })

  val addTokenLength = SentenceFeature("TokenFeatures.addTokenLength",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val tokenLength = token.string.length
      val tokenLengthTag = tokenLength match {
        case 0 => "0"
        case 1 => "1"
        case 2 => "2"
        case 3 => "3"
        case 4 => "4"
        case 5 => "5"
        case x if x > 5 && x < 10 => "INTERMEDIATE"
        case _ => "BIG"
      }
      Vector(s"TOKEN_LENGTH=" + tokenLengthTag)
    })


  val addTokenRelativePosition = SentenceFeature("TokenFeatures.addTokenRelativePosition",
    FeatureFunctionUtils.toBatchFn { (token: Token) =>
      val tokenCount = token.document.tokenCount
      //Quantize the relative position
      val tokenRelPos = token.position.toDouble / tokenCount
      val tokenRelPosTag = tokenRelPos match {
        case x if x < 0.25 => "Q1"
        case x if x >= 0.25 && x < 0.5 => "Q2"
        case x if x >= 0.5 && x < 0.75 => "Q3"
        case x if x >= 0.75 => "Q4"
      }
      Vector(s"TOKEN_RelPos=" + tokenRelPosTag)
    })

}
