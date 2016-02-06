package com.pchandar.app

import java.util.concurrent.{ExecutorService, Executors}

import com.pchandar.utils.{FGenConfig, FileUtils}
import org.slf4j.LoggerFactory

import scalaz.concurrent.{Strategy, Task}
import scalaz.stream.{Process, merge}

object LoadDatasetToMongo {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def loadDocumentsFromFileToMongo(config: FGenConfig): Unit = {
    implicit val pool: ExecutorService = Executors.newFixedThreadPool(config.threadSize)
    implicit val strategy = Strategy.Executor(pool)

    merge.mergeN(config.queueSize)(FileUtils.processDirectory(config.dataSet.getDataSetDirectory) map { i =>
      Process.eval(Task {
        val id = i.getName
        logger.debug(s"GeneratingFeature Start $id")
        try {
          config.dataSet.writeToMongo(config.dataSet.readDocumentFromFile(i.toURI.toURL))
        } catch {
          case e: Exception =>
            logger.error(s"GeneratingFeature Failed $id", e)
        }
      })
    }).runLog.unsafePerformSync
    pool.shutdown()
  }


}
