package com.pchandar.features

import cc.factorie.app.nlp.Token
import cc.factorie.app.strings._

object TimeTokenFeatures {


  val addAMPM = SentenceFeature("TimeTokenFeatures.addDigitTime", FeatureFunctionUtils.toBatchFn { (token: Token) =>
    val rawWord = token.string
    val word = simplifyDigits(rawWord).toLowerCase
    if (word.matches("((\\d+a|\\d+p)|(a\\.|p\\.|a\\.m|p\\.m|am|pm|a\\.m\\.|p\\.m\\.|am\\.|pm\\.))") && word != ".")
      Vector("AMPM")
    else
      Vector()
  })


  val addDigit = SentenceFeature("TimeTokenFeatures.addDigit", FeatureFunctionUtils.toBatchFn { (token: Token) =>
    if (token.isDigits) Vector("DIGIT") else Vector()
  })


  val addDigitTime = SentenceFeature("TimeTokenFeatures.addDigitTime", FeatureFunctionUtils.toBatchFn { (token: Token) =>
    if (token.string.matches("\\d+(:\\d+)?(a|p|am|pm|a\\.m|p\\.m|a\\.m\\.|p\\.m\\.)?") && token.string != ".")
      Vector("DIGIT_TIME")
    else
      Vector()
  })


}
