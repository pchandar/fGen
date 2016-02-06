package com.pchandar.load.ctgov

import java.io.File
import java.net.URL

import cc.factorie.app.nlp.Document
import com.mongodb.DBObject
import com.pchandar.load.DataSet
import com.pchandar.utils.MongoUtils
import play.api.libs.json.Json

case class ClinialTrialGovDocument(
                                    ntcid: String,
                                    title: String
                                  )

object ClinialTrialGovDocument {
  implicit val jsonFormat = Json.format[ClinialTrialGovDocument]
}

class ClinialTrialGovDataset(directory: String) extends DataSet[ClinialTrialGovDocument] {


  def readDocumentFromMongo(dbo: DBObject): ClinialTrialGovDocument =
    MongoUtils.getObjectFromMongo[ClinialTrialGovDocument](dbo)

  def readDocumentFromFile(path: URL): ClinialTrialGovDocument = {
    val xml = new CTGovTrialXML(new File(path.getPath))
    ClinialTrialGovDocument(xml.nct_id, xml.title)
  }

  def getDocument(doc: ClinialTrialGovDocument): Document = new Document(doc.title)

  def writeToMongo(doc: ClinialTrialGovDocument) = MongoUtils.convertToDBObject[ClinialTrialGovDocument](doc)

  override def getDataSetDirectory: Option[String] = Some(directory)

}
