package com.pchandar.load.ctgov

import java.io.File

import scala.xml.XML

/**
  * Parser for the CTGov Eligibility Criteria XML File
  *
  */
class CTGovTrialXML(filepath: File) {
  val xml = XML.loadFile(filepath)
  val root = (xml \\ "clinical_study")

  def nct_id = (root \\ "id_info" \\ "nct_id") text

  def title = (root \\ "brief_title") text

  def study_type = (root \\ "study_type") text

  def conditions = (for (n <- xml.child if n.label == "condition") yield n.text).mkString(",")

  def start_date = (root \\ "start_date") text

  def firstreceived_date = (root \\ "firstreceived_date") text

  def verification_date = (root \\ "verification_date") text

  def lastchanged_date = (root \\ "lastchanged_date") text

  def completion_date = (root \\ "completion_date") text

  // Extracts the Eligibility Criteria Free-Text
  def ec_min_age = (root \\ "eligibility" \\ "minimum_age") text

  def ec_max_age = (root \ "eligibility" \\ "maximum_age") text

  def ec_gender_age = (root \\ "eligibility" \\ "gender") text

  lazy val ec_text: String = {
    val study_pop = (root \\ "eligibility" \\ "study_pop") text
    val criteria = (root \\ "eligibility" \\ "criteria") text
    val ec_string = study_pop.trim + "\n" + criteria.trim
    ec_string
  }

  def ec_text_seperated: (String, String) = {

    val pattern_i_e = """(?s).*([iI]nclusion\s*[Cc]riteria)(.*)[eE]xclusion\s*[Cc]riteria(.*)""".r
    val pattern_e_i = """(?s).*([eE]xclusion\s*[Cc]riteria)(.*)[iI]nclusion\s*[Cc]riteria(.*)""".r

    ec_text.mkString match {
      case pattern_i_e(label, text1, text2) => (text1.trim, text2.trim)
      case pattern_e_i(label, text1, text2) => (text1.trim, text2.trim)
      case _ => null
    }
  }


}

