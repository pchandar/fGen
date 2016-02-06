package com.pchandar.app

import java.io.File

import com.pchandar.medical.UMLS
import com.pchandar.nlp.NLPResources
import com.pchandar.utils.FGenConfig
import com.typesafe.config.{Config, ConfigFactory}
import org.rogach.scallop.LazyScallopConf
import org.rogach.scallop.exceptions.ScallopException
import org.slf4j.LoggerFactory

object Boot {
  val logger = LoggerFactory.getLogger(getClass.getName)


  def main(args: Array[String]): Unit = {
    try {
      logger.info("Start of fGen")

      val opts = new LazyScallopConf(args) {
        banner("""Example: com.pchandar.app.Boot -action [umls|generate] -config [configClass]""".stripMargin)
        val action = opt[String]("action", short = 'a', required = true, descr = "create umls or generate features")
        val config = opt[String]("config", short = 'c', required = false, descr = "config class")
        val help = opt[Boolean]("help", noshort = true, descr = "Show this message")
      }

      opts.initialize {
        case ScallopException(message) =>
          logger.error(message)
          opts.printHelp()
          System.exit(1)
      }

      opts.action() match {
        case "umls" =>
          assert(new File(opts.config()).exists(), "Config file path does not exsist")
          val umls = new UMLS(new NLPResources)
          val config: Config = ConfigFactory.parseFile(new File(opts.config()))
          umls.build(config.getString("hashMapPath"), config.getString("umlsDatasetPath"))

        case "generateFromMongo" =>
          GenerateFeatures.run(FGenConfig.getConfigFromString(opts.config()))

        case "loadData" =>
          LoadDatasetToMongo.loadDocumentsFromFileToMongo(FGenConfig.getConfigFromString(opts.config()))

      }

    } catch {
      case e: Throwable => logger.error("caught error in Boot#main: " + e); throw e
    } finally {
      logger.debug("finally block reached. exiting program.")
    }
    logger.info("End of fGen!!!")
  }

}
