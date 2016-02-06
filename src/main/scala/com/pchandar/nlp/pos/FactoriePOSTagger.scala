package com.pchandar.nlp.pos

import cc.factorie.app.nlp.Document

// Wrapper Class Around Factorie's POSTagger
// TODO: Add Other POSTaggers such as Stanford, ClearNLP for easier comparison
class FactoriePOSTagger(posTagger: cc.factorie.app.nlp.pos.OntonotesForwardPosTagger) {
  def process(doc: Document): Document = posTagger.process(doc)
}
