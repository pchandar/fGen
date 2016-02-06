package com.pchandar.nlp.ner

import java.io._

import cc.factorie.app.chain.ChainModel
import cc.factorie.app.nlp._
import cc.factorie.app.nlp.ner.NerTag
import cc.factorie.variable._
import com.pchandar.features.FeatureGenerators

import scala.reflect.ClassTag

abstract class DetectionCRF[featureClass <: CategoricalVectorVar[String],
labelClass <: NerTag](val labelDomain: CategoricalDomain[String],
                      val featureDomain: CategoricalVectorDomain[String],
                      modelSource: InputStream = null)(implicit f: ClassTag[featureClass]) {

  def initializeFeatures(document: Document): Unit

  def getFeatureObject(token: Token): BinaryFeatureVectorVariable[String]

  class ChainNERModel(featuresDomain: CategoricalVectorDomain[String],
                      labelToFeatures: labelClass => featureClass,
                      labelToToken: labelClass => Token,
                      tokenToLabel: Token => labelClass)(implicit n: ClassTag[labelClass])
    extends ChainModel[labelClass, featureClass, Token](labelDomain, featuresDomain, labelToFeatures, labelToToken, tokenToLabel)


  val model: ChainModel[labelClass, featureClass, Token]

  def freeze(): Unit = featureDomain.freeze()


  def addFeatures(featureGenerators: FeatureGenerators)
                 (document: Document, features: (Token) => CategoricalVectorVar[String]): Unit = {
    featureGenerators.documentFeatures.foreach { documentFeatureGenerators =>
      val out = documentFeatureGenerators.f(document)
      document.tokens.foreach { token =>
        out(token).foreach(features(token) += _)
      }
    }

    featureGenerators.sentenceFeatures.foreach { sentenceFeatureGenerator =>
      document.sentences.foreach {
        sentence =>
          val out = sentenceFeatureGenerator.f(sentence.tokens)
          sentence.tokens.foreach { token =>
            out(token).foreach(features(token) += _)
          }

      }
    }

    document.sentences.foreach { sentence =>
      featureGenerators.factorieFeatures.foreach(_.f(sentence.tokens, features))
    }
  }
}


