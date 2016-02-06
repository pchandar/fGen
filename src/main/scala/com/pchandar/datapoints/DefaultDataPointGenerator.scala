package com.pchandar.datapoints

import cc.factorie.app.nlp.{Document, Token}
import cc.factorie.variable.LabeledMutableDiscreteVar
import com.pchandar.features.FeatureGenerators
import com.pchandar.nlp.NLPResources
import com.pchandar.nlp.ner.{FGenNER, FGenNERFeatures, FactorieNERTagger, LabeledFGenNERTag}
import com.pchandar.nlp.pos.FactoriePOSTagger
import com.pchandar.nlp.segment.{RuleBasedSentenceSegmenter, RuleBasedTokenizer}

class DefaultDataPointGenerator(nlpResources: NLPResources,
                                featureGenerators: FeatureGenerators) extends DataPointGenerator {

  val tokenizer = new RuleBasedTokenizer
  val sentenceSegmenter = new RuleBasedSentenceSegmenter
  val posTagger = new FactoriePOSTagger(nlpResources.posTagger)
  val nerTagger = new FactorieNERTagger(nlpResources.nerTagger)
  val model = new FGenNER(featureGenerators)

  def getDataPoint(id: String, doc: Document): IndexedSeq[LabeledDataPoint] = {
    model.initializeFeatures(doc)
    model.addFeatures(featureGenerators)(doc, (t: Token) => t.attr[FGenNERFeatures])
    doc.sentences.filter(_.length > 1).map(sentence => {
      LabeledDataPoint(id, sentence.tokens.map(_.attr[LabeledFGenNERTag with LabeledMutableDiscreteVar]).toSeq)
    }).toIndexedSeq
  }

  def finish(): Unit = model.featureDomain.freeze()
}

