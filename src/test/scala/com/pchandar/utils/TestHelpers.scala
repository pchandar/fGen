package com.pchandar.utils

import cc.factorie.app.nlp.Document
import com.pchandar.nlp.NLPResources
import com.pchandar.nlp.segment.RuleBasedTokenizer

object TestHelpers {

  val nlpResources = new NLPResources("lexiconTest/")

  def createDocumentWithTokens(text: String): Document = {
    val tokenizer = new RuleBasedTokenizer()
    val doc = new Document(text)
    tokenizer.process(doc)
    doc
  }
}
