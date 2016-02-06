package com.pchandar.load.ctgov

import scala.xml.XML


/**
  * Parser for the CTGov Eligibility Criteria - Sentence Dataset
  *
  * Options include
  * withLabel:
  * when the line dataset also contain labels use this flag to read the label for the sentence
  *
  */

class CTGovSentenceParser(sentenceString: String) {
  val header = "<?xml version=\"1.0\" encoding=\"UTF-16\" ?> "
  val xml = XML.loadString(header + sentenceString)

  def nct_id = (xml \\ "s" \ "@trail_id") text

  def ec_type = (xml \\ "s" \ "@type") text

  def sent_id = (xml \\ "s" \ "@sentID") text

  def text = (xml \\ "s") text

  def label = (xml \\ "s" \ "@label") text

}
