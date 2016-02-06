package com.pchandar.load

import java.net.URL

import cc.factorie.app.nlp.Document
import com.mongodb.Bytes._
import com.mongodb.DBObject
import com.mongodb.casbah.MongoCollection
import org.slf4j.LoggerFactory

trait DataSetSourceType

object MongoDataSet extends DataSetSourceType

object FileDataSet extends DataSetSourceType

trait DataSet[DocumentType] {
  val logger = LoggerFactory.getLogger(getClass.getName)
  val name: String = getClass.getName

  def mongoDocIterator(limit: Int = 0): (MongoCollection) => Iterator[DBObject] = {
    (coll: MongoCollection) => {
      val mongoCursor = if (limit == 0) coll.find().limit(limit) else coll.find()
      mongoCursor.underlying.addOption(QUERYOPTION_NOTIMEOUT)
      mongoCursor
    }
  }

  def getDataSetCollection: Option[MongoCollection] = {
    logger.error("No defined collection for this dataset")
    None
  }

  def getDataSetDirectory: Option[String] = {
    logger.error("No defined directory for this dataset")
    None
  }

  def getDocument(doc: DocumentType): Document

  def writeToMongo(doc: DocumentType)

  def readDocumentFromFile(folderPath: URL): DocumentType

  def readDocumentFromMongo(dbo: DBObject): DocumentType
}
