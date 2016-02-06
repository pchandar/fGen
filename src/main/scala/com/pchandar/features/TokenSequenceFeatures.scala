package com.pchandar.features

import cc.factorie.app.nlp.{Document, Token}
import cc.factorie.app.strings._
import cc.factorie.variable.CategoricalVectorVar

object TokenSequenceFeatures {

  // Factorie Featuers
  val addNeighbouring = FactorieFeature("TokenSequenceFeatures.addNeighbouring", {
    (tokenSequence: IndexedSeq[Token], vf: Token => CategoricalVectorVar[String]) =>

    cc.factorie.app.chain.Observations.addNeighboringFeatureConjunctions(
      tokenSequence,
      vf,
      "^[^@]*$",
      List(0),
      List(1),
      List(2),
      List(-1),
      List(-2)
    )
  })


  // Custom Features
  // Add features from window of 4 words before and after
  def addPrevTokenWindow(windowSize: Int = 4) = DocumentFeature("TokenSequenceFeatures.addPrevTokenWindow", { (document: Document) =>
    val tokenSequence = document.tokens
    tokenSequence.map(t =>
      (t, t.prevWindow(windowSize).map(t2 => "PREVWINDOW=" + simplifyDigits(t2.string).toLowerCase))
    ).toMap
  })


  def addNextTokenWindow(windowSize: Int = 4) = DocumentFeature("TokenSequenceFeatures.addNextTokenWindow", { (document: Document) =>
    val tokenSequence = document.tokens
    tokenSequence.map(t =>
      (t, t.nextWindow(windowSize).map(t2 => "NEXTWINDOW=" + simplifyDigits(t2.string).toLowerCase))
    ).toMap
  })


  def addCharNGrams(nGramLimit: Int = 5) = DocumentFeature("TokenSequenceFeatures.addCharNGrams", { (document: Document) =>
    val tokenSequence = document.tokens
    tokenSequence.map(t =>
      (t,
        if (t.string.matches("[A-Za-z]+"))
          t.charNGrams(2, nGramLimit).map(n => "NGRAM=" + n)
        else
          Vector()
        )
    ).toMap
  })





}
