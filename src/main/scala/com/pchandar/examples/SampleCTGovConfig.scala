package com.pchandar.examples

import java.io.File

import com.pchandar.datapoints.{DataPointGenerator, DefaultDataPointGenerator}
import com.pchandar.load.ctgov.{ClinialTrialGovDocument, ClinialTrialGovDataset}
import com.pchandar.nlp.NLPResources
import com.pchandar.nlp.ner.FGenNERFeatureGenerators
import com.pchandar.utils.FGenConfig


class SampleCTGovConfig extends FGenConfig {
  type DocumentType = ClinialTrialGovDocument
  val limit: Int = 100

  //override val outputColl: MongoCollection = MongoUtils.getMongoCollection("outputDB", "outputColl")
  override val outputFolder = {
    val path = workingDir + "/sampleCTGov"
    if (!new File(path).exists()) new File(path).mkdir()
    Some(new File(path).getAbsolutePath)
  }


  val dataSet = new ClinialTrialGovDataset(getClass.getClassLoader.getResource("ctgovTest").getPath)

  val nLPResources = new NLPResources
  // Using deafault feature here but it can be easily extended to use experimental features
  val defaultFeature = new FGenNERFeatureGenerators(nLPResources)
  val dataPointGenerator: DataPointGenerator = new DefaultDataPointGenerator(nLPResources,
    defaultFeature.featureGenerator)
}
