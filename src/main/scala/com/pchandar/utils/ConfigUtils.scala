package com.pchandar.utils

import com.typesafe.config.{Config, ConfigException}

import scala.collection.JavaConverters._

object ConfigUtils {

    // Extending typesafe config to work with optional configurations
    implicit class RichConfig(config: Config) {
      val getStringListOpt = getOpt(config.getStringList)

      val getDoubleListOpt = getOpt(config.getDoubleList)

      val getIntListOpt = getOpt(config.getIntList)

      val getStringOpt = getOpt(config.getString)

      val getIntOpt = getOpt(config.getInt)

      val getDoubleOpt = getOpt(config.getDouble)

      val getBooleanOpt = getOpt(config.getBoolean)

      def getListOfStrings(label: String): List[String] = config.getStringList(label).asScala.toList

      private def getOpt[T](f: String => T): String => Option[T] = { label =>
        try {
          Some(f(label))
        } catch {
          case _: ConfigException.Missing => None
        }
      }
    }

}
