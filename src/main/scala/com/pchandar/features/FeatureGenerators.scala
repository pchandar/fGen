package com.pchandar.features

import cc.factorie.app.nlp.{Document, Token}
import cc.factorie.variable.CategoricalVectorVar

case class SentenceFeature(name: String, f: IndexedSeq[Token] => Map[Token, Iterable[String]])
case class DocumentFeature(name: String, f: Document => Map[Token, Iterable[String]])
case class FactorieFeature(name: String, f: (IndexedSeq[Token], Token => CategoricalVectorVar[String]) => Unit)

case class FeatureGenerators(sentenceFeatures: List[SentenceFeature],
                             documentFeatures: List[DocumentFeature],
                             factorieFeatures: List[FactorieFeature]) {

  def addSentenceFeature(sentenceFeature: SentenceFeature) =
    this.copy(sentenceFeatures = sentenceFeature :: sentenceFeatures)

  def removeSentenceFeature(sentenceFeatureName: String) = {
    require(sentenceFeatures.map(_.name).contains(sentenceFeatureName))
    this.copy(sentenceFeatures = sentenceFeatures.filterNot(_.name == sentenceFeatureName))
  }

  def addFactorieFeature(factorieFeature: FactorieFeature) =
    this.copy(factorieFeatures = factorieFeature :: factorieFeatures)

  def removeFactorieFeature(factorieFeatureName: String) = {
    require(factorieFeatures.map(_.name).contains(factorieFeatureName))
    this.copy(factorieFeatures = factorieFeatures.filterNot(_.name == factorieFeatureName))
  }

  def addDocumentFeature(documentFeature: DocumentFeature) =
    this.copy(documentFeatures = documentFeature :: documentFeatures)

  def removeDocumentFeature(documentFeatureName: String) = {
    require(documentFeatures.map(_.name).contains(documentFeatureName))
    this.copy(documentFeatures = documentFeatures.filterNot(_.name == documentFeatureName))
  }
}

