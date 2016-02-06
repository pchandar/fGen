package com.pchandar.app

import java.io.{PrintWriter, File}
import java.util.concurrent.{ExecutorService, Executors}

import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.Implicits._
import com.pchandar.datapoints.LabeledDataPoint
import com.pchandar.load.MongoDataSet
import com.pchandar.utils.{FGenConfig, FileUtils, MongoUtils}
import org.slf4j.LoggerFactory

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream._

object GenerateFeatures {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def run(config: FGenConfig): Unit = {
    implicit val pool: ExecutorService = Executors.newFixedThreadPool(config.threadSize)
    implicit val strategy = Strategy.Executor(pool)

    if (config.datasetSourceType == MongoDataSet){
      assert(config.outputCollection.isDefined, "Output Collection not defined")
      writeToMongo(loadDocumentsFromMongo(config), config.outputCollection.get)
    }
    else{
      assert(config.outputFolder.isDefined, "Output Folder is not defined")
      writeToFile(loadDocumentsFromFolder(config), config.outputFolder.get)
    }

    pool.shutdown()
  }

  def loadDocumentsFromMongo(config: FGenConfig)(implicit pool: ExecutorService): IndexedSeq[LabeledDataPoint] = {

    merge.mergeN(config.queueSize)(
      MongoUtils.processCollection
      (config.dataSet.getDataSetCollection)
      (config.dataSet.mongoDocIterator(config.limit)) map { i =>
        Process.eval(Task {
          val id = i.getAs[String]("id").getOrElse("UNK_DOCID")
          logger.debug(s"GeneratingFeature Start $id")
          try {
            val datasetDoc = config.dataSet.readDocumentFromMongo(i)
            val nlpDoc = config.dataSet.getDocument(datasetDoc)
            val dataPoints = config.dataPointGenerator.getDataPoint(id, nlpDoc)
            dataPoints
          } catch {
            case e: Exception =>
              logger.error(s"GeneratingFeature Failed $id", e)
              IndexedSeq[LabeledDataPoint]()
          }
        })
      }).runLog.unsafePerformSync.flatten

  }


  def loadDocumentsFromFolder(config: FGenConfig)(implicit pool: ExecutorService): IndexedSeq[LabeledDataPoint] = {
    println("GeneratingFeature")
    merge.mergeN(config.queueSize)(FileUtils.processDirectory(config.dataSet.getDataSetDirectory) map { i =>
      Process.eval(Task {
        val id = i.getName
        logger.debug(s"GeneratingFeature Start $id")
        try {
          println(id)
          val datasetDoc = config.dataSet.readDocumentFromFile(i.getAbsoluteFile.toURI.toURL)
          val nlpDoc = config.dataSet.getDocument(datasetDoc)
          val dataPoints = config.dataPointGenerator.getDataPoint(id, nlpDoc)
          dataPoints
        } catch {
          case e: Exception =>
            logger.error(s"GeneratingFeature Failed $id", e)
            IndexedSeq[LabeledDataPoint]()
        }
      })
    }).runLog.unsafePerformSync.flatten
  }

  // TODO: Implement this
  def writeToMongo(dataPoints: IndexedSeq[LabeledDataPoint], outputFolder: MongoCollection): Unit = ???

  def writeToFile(dataPoints: IndexedSeq[LabeledDataPoint], outputFolder: String): Unit = {
    dataPoints foreach { dp =>
      val pw = new PrintWriter(new File(outputFolder + "/" + dp.id + ".features" ))
      dp.vec.toVector foreach { vector =>
        pw.write(vector.token.string + " " + vector.value.activeElements.map(t => t._1 +":" + t._2).mkString(" ") + "\n")
      }
      pw.close()
    }
  }


}
