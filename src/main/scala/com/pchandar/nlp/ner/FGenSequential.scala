package com.pchandar.nlp.ner

import java.io._

import cc.factorie.app.chain.ChainModel
import cc.factorie.app.nlp.ner.{BILOU, NerSpan, NerSpanLabel, NerTag}
import cc.factorie.app.nlp.{Document, Token, _}
import cc.factorie.util.BinarySerializer
import cc.factorie.variable._
import com.pchandar.features._
import com.pchandar.nlp.NLPResources


// Label Classes (Features and Domain)
object BaseFGenSequentialLabelDomain extends EnumDomain {
  val O, NER = Value
  freeze()
}

object FGenSequentialLabelDomain extends CategoricalDomain[String] with BILOU {
  this ++= encodedTags(BaseFGenSequentialLabelDomain.categories)
  freeze()

  def spanList(section: Section): FGenSequentialSpanBuffer = {
    val boundaries = NerUtils.multiclassBoundaries(section.tokens.map(_.attr[FGenSequentialTag].categoryValue))
    new FGenSequentialSpanBuffer ++= boundaries.map(b => new FGenSequentialSpan(section, b._1, b._2, b._3))
  }
}

class FGenSequentialSpanBuffer extends TokenSpanBuffer[FGenSequentialSpan]

// Tag Classes
// TODO: The problem with this approach is the domain/ label classes for the sequence
//      labelling problem can't be changed dynamically FIX THIS
class FGenSequentialTag(token: Token, initialCategory: String)
  extends NerTag(token, initialCategory) {
  def domain = FGenSequentialLabelDomain
}

class LabeledFGenSequentialTag(token: Token, initialCategory: String)
  extends FGenSequentialTag(token, initialCategory) with CategoricalLabeling[String]


// Span Classes
class FGenSequentialSpanLabel(span: TokenSpan, initialCategory: String) extends NerSpanLabel(span, initialCategory) {
  def domain = FGenSequentialLabelDomain
}

class FGenSequentialSpan(section: Section, start: Int, length: Int, category: String) extends NerSpan(section, start, length) {
  val label = new FGenSequentialSpanLabel(this, category)
}


// Feature Classes (Features and Domain)
object FGenSequentialFeaturesDomain extends CategoricalVectorDomain[String]

class FGenSequentialFeatures(val token: Token) extends BinaryFeatureVectorVariable[String] {
  def domain = FGenSequentialFeaturesDomain

  override def skipNonCategories = true
}

case class FGenNERModel(crfModel: ChainModel[FGenSequentialTag, FGenSequentialFeatures, Token])

object FGenSequential {
  type LabelClass = FGenSequentialTag
  type FeatureClass = FGenSequentialFeatures
  val labelToToken = (l: FGenSequentialTag) => l.token

  def chainModelFactory = new ChainModel[LabelClass, FeatureClass, Token](
    FGenSequentialLabelDomain, FGenSequentialFeaturesDomain,
    l => labelToToken(l).attr[FeatureClass],
    labelToToken,
    t => t.attr[LabelClass])

  def deserialize(stream: java.io.InputStream): FGenNERModel = {
    import cc.factorie.util.CubbieConversions._
    val model = chainModelFactory
    val is = new DataInputStream(new BufferedInputStream(stream))
    BinarySerializer.deserialize(FGenSequentialFeaturesDomain.dimensionDomain, is)
    BinarySerializer.deserialize(model, is)
    is.close()
    FGenNERModel(model)
  }


  def serialize(model: ChainModel[LabelClass, FeatureClass, Token], stream: java.io.OutputStream): Unit = {
    import cc.factorie.util.CubbieConversions._
    val is = new DataOutputStream(new BufferedOutputStream(stream))
    BinarySerializer.serialize(FGenSequentialFeaturesDomain.dimensionDomain, is)
    BinarySerializer.serialize(model, is)
    is.close()
  }
}


class FGenSequential(featureGenerator: FeatureGenerators, _model: FGenNERModel = null) extends DetectionCRF[FGenSequentialFeatures, FGenSequentialTag](FGenSequentialLabelDomain, FGenSequentialFeaturesDomain) {

  val newLabel = (t: Token, s: String) => new FGenSequentialTag(t, s)
  val labelToToken = (l: FGenSequentialTag) => l.token


  def getFeatureObject(token: Token) = new FGenSequentialFeatures(token)


  val model = if(_model == null) FGenSequential.chainModelFactory else _model.crfModel

  def initializeFeatures(document: Document): Unit = {
    if (!document.tokens.head.attr.contains(classOf[FGenSequentialFeatures])) {
      document.tokens.map(token => {
        token.attr += new FGenSequentialFeatures(token)
      })
    }
  }


  def process(document: Document): Document = {
    if (document.tokenCount == 0) return document
    if (!document.tokens.head.attr.contains(classOf[FGenSequentialTag]))
      document.tokens.map(token => token.attr += newLabel(token, "O"))

    initializeFeatures(document)
    addFeatures(featureGenerator)(document, (t: Token) => t.attr[FGenSequentialFeatures])

    for (sentence <- document.sentences if sentence.tokens.nonEmpty) {
      val vars = sentence.tokens.map(_.attr[FGenSequentialTag]).toSeq
      model.maximize(vars)(null)
    }

    document.attr.+=(new FGenSequentialSpanBuffer
      ++= document.sections.flatMap(section => FGenSequentialLabelDomain.spanList(section)))

    document
  }

  override def freeze() = FGenSequentialFeaturesDomain.freeze()
}

class FGenNERFeatureGenerators(resource: NLPResources) {

  val lexiconFeatures = new LexiconFeatures(resource)


  val sentenceFeatureGenerators =
    List(
      TimeTokenFeatures.addAMPM,
      TimeTokenFeatures.addDigit,
      TimeTokenFeatures.addDigitTime,
      TokenFeatures.addPrefixSuffix,
      TokenFeatures.addSimplifyDigitsWord,
      TokenFeatures.addShape,
      //TokenFeatures.addWordNetWord,
      TokenFeatures.addisPunctuation,
      TokenFeatures.addContainsPrimeS,
      TokenFeatures.addTokenLength,
      //TokenFeatures.addSimplifiedPOSTagger,
      TokenFeatures.addTokenRelativePosition
    )

  val documentFeatureGenerators =
    List(
      TokenSequenceFeatures.addPrevTokenWindow(),
      TokenSequenceFeatures.addNextTokenWindow(),
      TokenSequenceFeatures.addCharNGrams()
    )

  val factorieFeatures =
    List(
      lexiconFeatures.addIELS,
      lexiconFeatures.addWikipedia,
      TokenSequenceFeatures.addNeighbouring
    )

  val featureGenerator = FeatureGenerators(sentenceFeatureGenerators, documentFeatureGenerators, factorieFeatures)

}

