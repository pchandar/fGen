package com.pchandar.utils

import java.io.File

import org.slf4j.LoggerFactory

import scalaz.concurrent.Task
import scalaz.stream._

object FileUtils {
  val logger = LoggerFactory.getLogger(getClass.getName)

  def processDirectory(datasetDirectory: Option[String]): Process[Task, File] =
    io.resource(Task(datasetDirectory))(c =>
      Task {
        logger.info(s"Cursor [$c] closed")
      }) { sourceCollection =>
      assert(datasetDirectory.isDefined, "Dataset directory must be defined")
      lazy val documents = new File(datasetDirectory.get).listFiles.toIterator
      Task {
        if (documents.hasNext)
          documents.next()
        else {
          logger.info("Done reading from source collection")
          throw Cause.Terminated(Cause.End)
        }
      }.handleWith {
        case e: Cause.Terminated =>
          Task.fail(e)
        case t: Throwable =>
          logger.error("Error reading from source collection", t)
          Task.fail(t)

      }
    }


}


