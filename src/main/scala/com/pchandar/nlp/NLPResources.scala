package com.pchandar.nlp

import cc.factorie.app.nlp.lexicon.{LexiconsProvider, StaticLexicons}
import cc.factorie.app.nlp.ner.ConllChainNer
import cc.factorie.app.nlp.pos.OntonotesForwardPosTagger
import cc.factorie.util.ModelProvider
import org.slf4j.LoggerFactory


class NLPResources(lexiconDirectory: String = "lexicon/") {
  val logger = LoggerFactory.getLogger(getClass.getName)

  // TODO: Lazy val is probably bad here (might lead to delayed failures
  lazy val allTheLexicons: StaticLexicons = {
    try {
      logger.info(s"Loading all Factorie Lexicons ...")
      val lexicons = new StaticLexicons()(
        LexiconsProvider.fromUrl(getClass.getClassLoader.getResource(lexiconDirectory)))
      logger.info(s"Loading of all Factorie Lexicons ... Done!!!")
      lexicons
    } catch {
      case e: Exception =>
        logger.error(s"Unable to load Factorie Lexicons (Check resources folder)")
        null
    }
  }


  lazy val posTagger: OntonotesForwardPosTagger = {
    try {
      logger.info(s"Loading OntonotesForwardPosTagger Model ...")
      val posTagger = new OntonotesForwardPosTagger(
        getClass.getClassLoader.getResource("models/OntonotesForwardPosTagger.model"))
      logger.info(s"Loading OntonotesForwardPosTagger Model ... Done!!!")
      posTagger
    } catch {
      case e: Exception =>
        logger.error(s"Unable to load OntonotesForwardPosTagger Model (Check resources folder)")
        null
    }
  }

  lazy val nerTagger: ConllChainNer = {
    try {
      logger.info(s"Loading ConllChainNer Model ...")
      val factorieNERTagger: ConllChainNer = new ConllChainNer()(ModelProvider.classpath[ConllChainNer](".model"),
        allTheLexicons)
      logger.info(s"Loading ConllChainNer Model ... Done!!!")
      factorieNERTagger
    } catch {
      case e: Exception =>
        logger.error(s"Unable to load ConllChainNer Model (Check resources folder)")
        null
    }
  }


  lazy val englishStopwords = {
    try {
      logger.info(s"Loading English Stopwords ...")
      val englishStopwords = getClass.getClassLoader.getResourceAsStream("stopwords/english.csv")
      logger.info(s"Loading English Stopwords ... Done!!!")
      englishStopwords
    } catch {
      case e: Exception =>
        logger.error(s"Unable to load Loading English Stopwords (Check resources folder)")
        null
    }
  }


  lazy val medicalStopwords = {
    try {
      logger.info(s"Loading Medical Stopwords ...")
      val medicalStopwords = getClass.getClassLoader.getResourceAsStream("stopwords/medical.csv")
      logger.info(s"Loading Medical Stopwords ... Done!!!")
      medicalStopwords
    } catch {
      case e: Exception =>
        logger.error(s"Unable to load Loading Medical Stopwords (Check resources folder)")
        null
    }
  }
}
