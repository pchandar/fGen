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
object BaseFGenNERLabelDomain extends EnumDomain {
  val O, NER = Value
  freeze()
}

object FGenNERLabelDomain extends CategoricalDomain[String] with BILOU {
  this ++= encodedTags(BaseFGenNERLabelDomain.categories)
  freeze()

  def spanList(section: Section): FGenNERSpanBuffer = {
    val boundaries = NerUtils.multiclassBoundaries(section.tokens.map(_.attr[FGenNERTag].categoryValue))
    new FGenNERSpanBuffer ++= boundaries.map(b => new FGenNERSpan(section, b._1, b._2, b._3))
  }
}

class FGenNERSpanBuffer extends TokenSpanBuffer[FGenNERSpan]

// Tag Classes
// TODO: The problem with this approach is the domain/ label classes for the sequence
//      labelling problem can't be changed dynamically FIX THIS
class FGenNERTag(token: Token, initialCategory: String)
  extends NerTag(token, initialCategory) {
  def domain = FGenNERLabelDomain
}

class LabeledFGenNERTag(token: Token, initialCategory: String)
  extends FGenNERTag(token, initialCategory) with CategoricalLabeling[String]


// Span Classes
class FGenNERSpanLabel(span: TokenSpan, initialCategory: String) extends NerSpanLabel(span, initialCategory) {
  def domain = FGenNERLabelDomain
}

class FGenNERSpan(section: Section, start: Int, length: Int, category: String) extends NerSpan(section, start, length) {
  val label = new FGenNERSpanLabel(this, category)
}


// Feature Classes (Features and Domain)
object FGenNERFeaturesDomain extends CategoricalVectorDomain[String]

class FGenNERFeatures(val token: Token) extends BinaryFeatureVectorVariable[String] {
  def domain = FGenNERFeaturesDomain

  override def skipNonCategories = true
}

case class FGenNERModel(crfModel: ChainModel[FGenNERTag, FGenNERFeatures, Token])

object FGenNER {
  type LabelClass = FGenNERTag
  type FeatureClass = FGenNERFeatures
  val labelToToken = (l: FGenNERTag) => l.token

  def chainModelFactory = new ChainModel[LabelClass, FeatureClass, Token](
    FGenNERLabelDomain, FGenNERFeaturesDomain,
    l => labelToToken(l).attr[FeatureClass],
    labelToToken,
    t => t.attr[LabelClass])

  def deserialize(stream: java.io.InputStream): FGenNERModel = {
    import cc.factorie.util.CubbieConversions._
    val model = chainModelFactory
    val is = new DataInputStream(new BufferedInputStream(stream))
    BinarySerializer.deserialize(FGenNERFeaturesDomain.dimensionDomain, is)
    BinarySerializer.deserialize(model, is)
    is.close()
    FGenNERModel(model)
  }


  def serialize(model: ChainModel[LabelClass, FeatureClass, Token], stream: java.io.OutputStream): Unit = {
    import cc.factorie.util.CubbieConversions._
    val is = new DataOutputStream(new BufferedOutputStream(stream))
    BinarySerializer.serialize(FGenNERFeaturesDomain.dimensionDomain, is)
    BinarySerializer.serialize(model, is)
    is.close()
  }
}


class FGenNER(featureGenerator: FeatureGenerators, _model: FGenNERModel = null) extends DetectionCRF[FGenNERFeatures, FGenNERTag](FGenNERLabelDomain, FGenNERFeaturesDomain) {

  val newLabel = (t: Token, s: String) => new FGenNERTag(t, s)
  val labelToToken = (l: FGenNERTag) => l.token


  def getFeatureObject(token: Token) = new FGenNERFeatures(token)


  val model = _model.crfModel

  def initializeFeatures(document: Document): Unit = {
    if (!document.tokens.head.attr.contains(classOf[FGenNERFeatures])) {
      document.tokens.map(token => {
        token.attr += new FGenNERFeatures(token)
      })
    }
  }


  def process(document: Document): Document = {
    if (document.tokenCount == 0) return document
    if (!document.tokens.head.attr.contains(classOf[FGenNERTag]))
      document.tokens.map(token => token.attr += newLabel(token, "O"))

    initializeFeatures(document)
    addFeatures(featureGenerator)(document, (t: Token) => t.attr[FGenNERFeatures])

    for (sentence <- document.sentences if sentence.tokens.nonEmpty) {
      val vars = sentence.tokens.map(_.attr[FGenNERTag]).toSeq
      model.maximize(vars)(null)
    }

    document.attr.+=(new FGenNERSpanBuffer
      ++= document.sections.flatMap(section => FGenNERLabelDomain.spanList(section)))

    document
  }

  override def freeze() = FGenNERFeaturesDomain.freeze()
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

