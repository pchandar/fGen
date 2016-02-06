package com.pchandar.nlp.segment

class LexerFactory {
  def makeToken(str: String, begin: Int, length: Int): (String, Int, Int) = {
    (str, begin, length)
  }
}
