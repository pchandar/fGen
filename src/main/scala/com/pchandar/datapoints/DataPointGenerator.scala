package com.pchandar.datapoints

import cc.factorie.app.nlp.Document
import com.pchandar.nlp.ner.FGenSequentialFeatures

case class LabeledDataPoint(id: String, vec: Seq[FGenSequentialFeatures]) extends Serializable

/**
  * FeatureVectorGenerators defines a way to convert an Document to a data point.
  */
trait DataPointGenerator {
  val name: String = this.getClass.getName

  /**
    * Given a document, the function generate a list of data points.
    *
    * @param doc Document is a normalized document obtained from a dataset
    * @return a list of datapoints
    */
  def getDataPoint(id: String, doc: Document): IndexedSeq[LabeledDataPoint]

  def finish(): Unit

}

