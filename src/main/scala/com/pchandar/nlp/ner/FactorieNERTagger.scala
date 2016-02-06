package com.pchandar.nlp.ner

import cc.factorie.app.nlp.Document

// Wrapper Class Around Factorie's Named Entity Tagger
// TODO: Add Other NER Tagger such as Stanford, ClearNLP, Washington for easier comparison
class FactorieNERTagger(nerTagger: cc.factorie.app.nlp.ner.ConllChainNer) {
  def process(doc: Document): Document = nerTagger.process(doc)
}
