package com.pchandar.utils

import java.io.File
import java.net.URL

import com.mongodb.casbah.MongoCollection
import com.pchandar.datapoints.DataPointGenerator
import com.pchandar.load.{FileDataSet, MongoDataSet, DataSetSourceType, DataSet}
import org.slf4j.LoggerFactory

trait FGenConfig {
  type DocumentType
  val threadSize: Int = 1
  val queueSize: Int = threadSize + 1
  val limit: Int

  val datasetSourceType: DataSetSourceType = FileDataSet

  val dataSet: DataSet[DocumentType]

  // Feature vector generator converts an document into a datapoint (see: LabeledExamples)
  val dataPointGenerator: DataPointGenerator


  //TODO Move this to output formats
  // Output to Mongo
  val outputCollection: Option[MongoCollection] = None
  val outputFolder: Option[String] = None


  // Output Path Details
  lazy val workingDir: String = {
    val dir = System.getProperty("user.home") + "/fGen"
    if (!new File(dir).exists()) new File(dir).mkdir()
    dir
  }
}

object FGenConfig {
  val logger = LoggerFactory.getLogger(getClass.getName)
  def getConfigFromString(className: String) = {
    logger.info(s"Loaded configuration from : {$className}")
    Class.forName(className).newInstance.asInstanceOf[FGenConfig]
  }
}

