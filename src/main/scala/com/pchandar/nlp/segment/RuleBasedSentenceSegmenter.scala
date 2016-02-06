package com.pchandar.nlp.segment

import cc.factorie.app.nlp._
import cc.factorie.app.nlp.segment.DeterministicRegexTokenizer

/** Segments a sequence of tokens into sentences. */
// NOTE: This file was copied from Factorie and modified
class RuleBasedSentenceSegmenter {

  /** If true, every newline causes a sentence break. */
  var newlineBoundary = false

  /** If true  every double newline causes a sentence break. */
  var doubleNewlineBoundary = true

  /** Matches the Token.string of punctuation that always indicates the end of a sentence.  It does not include possible additional tokens that may be appended to the sentence such as quotes and closing parentheses. */
  val closingRegex = "\\A([!\\?]+|[\\.;])\\Z".r // We allow repeated "!" and "?" to end a sentence, but not repeated "."

  /** Matches the Token.string of tokens that may extend a sentence, such as quotes, closing parentheses, and even additional periods. */
  val closingContinuationRegex = "^''|[\\.\"!\\?\\p{Pf}\\p{Pe}]+$".r

  /** Matches the Token.string of tokens that might possibility indicate the end of a sentence, such as an mdash.
      *The sentence segmenter will only actually create a sentence end here if possibleSentenceStart is true for the following token. */
  val possibleClosingRegex = "^\\.\\.+|[-\\p{Pd}\u2014]+$".r

  /** Whitespace that should not be allowed between a closingRegex and closingContinuationRegex for a sentence continuation.  For example:  He ran.  "You shouldn't run!" */
  val spaceRegex = "[ \n\r\t\u00A0\\p{Z}]+".r

  val emoticonRegex = ("\\A(" + DeterministicRegexTokenizer.emoticon + ")\\Z").r

  /** If there are more than this number of characters between the end of the previous token and the beginning of this one, force a sentence start.
      *If negative, don't break sentences according to this criteria at all. */
  val charOffsetBoundary = 10

  /** Returns true for strings that probably start a sentence after a word that ends with a period. */
  // Consider adding more honorifics and others here. -akm
  def possibleSentenceStart(s: String): Boolean =
    java.lang.Character.isUpperCase(s(0)) &&
      (cc.factorie.app.nlp.lexicon.StopWords.containsWord(s) || s == "Mr." || s == "Mrs." ||
        s == "Ms." || s == "\"" || s == "''")



  def process(document: Document): Document = {
    for (section <- document.sections) {
      val tokens = section.tokens
      var i = 0
      var sentenceStart = 0

      // Create a new Sentence, register it with the section, and update sentenceStart.  Here sentenceEnd is non-inclusive
      def newSentence(sentenceEnd: Int): Unit = {
        if (sentenceStart != sentenceEnd)
          new Sentence(section, sentenceStart, sentenceEnd - sentenceStart)
        sentenceStart = sentenceEnd
      }

      while (i < tokens.length) {
        val token = tokens(i)
        val string = tokens(i).string

        // Sentence boundary from a single newline
        if (newlineBoundary && i > sentenceStart && i > 0 &&
          document.string.substring(tokens(i - 1).stringEnd, token.stringStart).contains('\n')) {
          newSentence(i)
        }
        // Sentence boundary from a double newline
        else if (doubleNewlineBoundary && i > sentenceStart && i > 0 &&
          document.string.substring(tokens(i - 1).stringEnd, token.stringStart).contains("\n\n")) {
          newSentence(i)
        }
        // Emoticons are single-token sentences
        else if (emoticonRegex.findFirstMatchIn(string).isDefined) {
          if (i > 0) newSentence(i)
          newSentence(i + 1)
        }
        // Sentence boundary from sentence-terminating punctuation
        else if (closingRegex.findFirstMatchIn(string).isDefined) {
          while (i + 1 < tokens.length &&
            spaceRegex.findFirstMatchIn(document.string.substring(token.stringEnd, tokens(i + 1).stringStart)).isDefined &&
            closingContinuationRegex.findPrefixMatchOf(tokens(i + 1).string).isDefined) i += 1
          // look for " or ) after the sentence-ending punctuation

          newSentence(i + 1)
          //Possible sentence boundary from a word that ends in '.'
          // For example:  "He left the U.S.  Then he came back again."
        } else if (string(string.length - 1) == '.') {
          // token ends in ., might also be end of sentence
          if (i + 1 < tokens.length && possibleSentenceStart(tokens(i + 1).string)) {
            // If the next word is a capitalized stopword, then say it is a sentence
            val t2 = new Token(token.stringEnd - 1, token.stringEnd)
            // Insert a token containing just the last (punctuation) character
            section.insert(i + 1, t2)
            i += 1
            newSentence(i + 1)
            // need to have this down here in case this was the first sentence
            t2._sentence = section.sentences.last
          }
          // Possible sentence boundary from the dash in something like LONDON - Today Prime Minister...
        } else if (possibleClosingRegex.findPrefixMatchOf(string).isDefined) {
          if (i + 1 < tokens.length && possibleSentenceStart(tokens(i + 1).string)) {
            newSentence(i + 1)
          }
        } // TODO The main way this can break a sentence incorrectly is when there is a long "<a href...>" tag.
        else if (charOffsetBoundary > 0 && i > sentenceStart &&
          token.hasPrev && token.stringStart - token.prev.stringEnd > charOffsetBoundary &&
          document.string.substring(token.prev.stringEnd, token.prev.stringEnd + 3).trim.toLowerCase != "<a") {

          newSentence(i)
        }
        i += 1
      }
      // Final sentence
      if (sentenceStart < tokens.length) newSentence(tokens.length)
      // Set each Token's internal record of its sentence, to avoid having to look it up later.
      for (sentence <- section.sentences; token <- sentence.tokens) token._sentence = sentence
    }
    if (!document.annotators.contains(classOf[Sentence]))
      document.annotators(classOf[Sentence]) = this.getClass
    document
  }

}

