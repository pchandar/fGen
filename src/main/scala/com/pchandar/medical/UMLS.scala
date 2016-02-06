package com.pchandar.medical

import java.io.File

import com.pchandar.nlp.NLPResources
import com.pchandar.nlp.segment.RuleBasedTokenizer
import net.openhft.chronicle.map.{ChronicleMap, ChronicleMapBuilder}

import scala.collection.mutable
import scala.io.Source

class UMLS(resources: NLPResources) {
  var map: ChronicleMap[String, scala.collection.Set[String]] = null
  var tui_map: ChronicleMap[String, Set[String]] = null
  var set_stop_eng = Set[String]()
  var set_stop_med = Set[String]()

  def build(mapPath: String, umlsDatasetPath: String): Unit = {
    build_umls_tuis(umlsDatasetPath, mapPath)
    build_umls_cuis(umlsDatasetPath, mapPath)
  }


  // Functions to build a UMLS HashMap from the UMLS Data


  def build_umls_tuis(umls_path: String, map_path: String): Unit = {
    val full_umls_path = umls_path + "/MRSTY.RRF"
    val filename_f: File = new File(map_path + "/tui.map")
    tui_map = ChronicleMapBuilder.of(classOf[String], classOf[Set[String]]).entries(3198788).averageKeySize(5).createPersistedTo(filename_f)
    val lines = Source.fromFile(full_umls_path, "ISO-8859-1")
    var x = 0
    for (line <- lines.getLines()) {
      x += 1
      add_MRSTY_line(line)
      if (x % 1000000 == 0) println(x + " lines processed")
    }

  }


  def build_umls_cuis(umls_path: String, map_path: String): Unit = {
    val full_umls_path = umls_path + "/MRCONSO.RRF"
    val filename_f: File = new File(map_path + "/cui.map")
    val tokenizer = new RuleBasedTokenizer
    map = ChronicleMapBuilder.of(classOf[String], classOf[scala.collection.Set[String]])
      .entries(11000000)
      .averageKeySize(10)
      .createPersistedTo(filename_f)

    val lines = Source.fromFile(full_umls_path, "ISO-8859-1")
    var x = 0
    for (line <- lines.getLines()) {
      x += 1
      add_MRCONSO_line(line, tokenizer)
      if (x % 1000000 == 0) println(x + " lines processed")

    }
  }

  private def add_MRSTY_line(line: String): Unit = {
    if (line == null) return
    val rows = line.split('|')
    val cui = rows(0).trim
    val tui = rows(1).trim

    // Check and add TUI for CUI
    val cui_map_val = tui_map.get(cui)
    if (cui_map_val != null) {
      cui_map_val.addString(new mutable.StringBuilder(tui))
      tui_map.put(cui, cui_map_val)
    }
    else tui_map.put(cui, Set[String](tui))
  }


  private def add_MRCONSO_line(line: String, tokenizer: RuleBasedTokenizer): Unit = {
    if (line == null) return
    val rows = line.split('|')
    val cui = rows(0)
    val lang = rows(1)
    val status = rows(2)
    val form = rows(4)
    val source = rows(11).trim
    val suppress = rows(16)
    val source_code_id = rows(13)
    val text = rows(14)
    val code_sources = Map[String, String]("ICD9CM" -> "I:", "RXNORM" -> "R:", "SNOMEDCT_US" -> "S:")
    if (source != "SNOMEDCT_US" && source != "ICD9CM" && source != "RXNORM" && source != "MEDLINEPLUS") return
    if (lang == "ENG" & suppress == "N") {
      // Tokenize the line
      val p_text = tokenizer.getTokenFromString(text)
      val p_text_str = preprocess(p_text.map(_.string).mkString(" "))
      if (p_text_str.split(" ").length > 10) return
      val cui_map_val = map.get(cui)
      if (status == "P" & form == "PF") {
        if (cui_map_val != null) {
          cui_map_val.addString(new mutable.StringBuilder(p_text_str))
          map.put(cui, cui_map_val)
        }
        else map.put(cui, Set[String](p_text_str))
      }

      if (code_sources.contains(source)) {
        if (cui_map_val != null) {
          cui_map_val.addString(new mutable.StringBuilder(code_sources.get(source).get + source_code_id))
          cui_map_val.addString(new mutable.StringBuilder("STR_" + code_sources.get(source).get + p_text_str))
          map.put(cui, cui_map_val)

        }
        else {
          map.put(cui, Set[String](code_sources.get(source).get + source_code_id))
          map.put(cui, Set[String]("STR_" + code_sources.get(source).get + p_text_str))
        }
      }

      val ptext_map_val = map.get(p_text_str)
      if (ptext_map_val != null) {
        ptext_map_val.addString(new mutable.StringBuilder(cui))
        map.put(p_text_str, ptext_map_val)
      }
      else map.put(p_text_str, Set[String](cui))
    }
  }


  def loadUMLSHashMap(map_path: String): Unit = {
    // Load Stop Words
    for (line <- Source.fromInputStream(resources.englishStopwords).getLines()) set_stop_eng += preprocess(line)
    for (line <- Source.fromInputStream(resources.medicalStopwords).getLines()) set_stop_med += preprocess(line)

    val filename_cui: File = new File(map_path + "/cui.map")
    map = ChronicleMapBuilder.of(classOf[String], classOf[scala.collection.Set[String]])
      .createPersistedTo(filename_cui)

    val filename_tui = new File(map_path + "/tui.map")
    tui_map = ChronicleMapBuilder.of(classOf[String], classOf[Set[String]]).createPersistedTo(filename_tui)
  }



  // Function Class to access the UMLS Data from the HashMap
  def getCUI(txt: List[String]): scala.collection.Set[String] = {
    val out = getCUI(preprocess(txt.mkString(" ")))
    if (out == null) Set[String]()
    else out
  }

  def getCUI(txt: String): scala.collection.Set[String] = {
    if (shouldFilter(txt.trim)) return Set[String]()
    val out = map.get(txt.trim)
    if (out == null) Set[String]()
    else out
  }

  def getTUI(cui: String): Set[String] = tui_map.get(cui)

  def getPrefTerm(cui: String): String = {
    var prfTerm = ""
    var snomed_term = ""
    val cuis = map.get(cui)

    if (cuis == null) return prfTerm
    else for (t <- cuis) {
      if (!t.startsWith("S")) prfTerm = t
      else if (t.startsWith("STR_S:")) snomed_term = t.replaceAll("STR_S:", "")
    }
    if (prfTerm != "") prfTerm
    else if (snomed_term != "") snomed_term
    else ""
  }


  // HELPER FUNCTIONS

  def shouldFilter(txt: String, onlyEng: Boolean = false): Boolean = {
    if (set_stop_eng == null | set_stop_med == null) return false
    val ptext = preprocess(txt)
    if (ptext.trim == "") return true

    if (ptext.split(" ").length == 1) {
      if (onlyEng) {
        if (set_stop_eng.contains(ptext)) return true
      }
      else {
        if (set_stop_eng.contains(ptext) | set_stop_med.contains(ptext)) return true
      }
    }
    else if (set_stop_eng.contains(ptext.split(" ")(0)) | set_stop_eng.contains(ptext.split(" ").last)) return true
    false
  }


  def preprocess(txt: String): String = {
    txt.replaceAll("[!\"#$%&()\\'*+,-./:;<=>?@[\\\\]^_`{|}~]", " ").replaceAll(" +", " ")
  }


}

