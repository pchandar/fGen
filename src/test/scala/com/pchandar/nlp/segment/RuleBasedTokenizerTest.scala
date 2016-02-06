package com.pchandar.nlp.segment

import cc.factorie.app.nlp.Document
import org.scalatest.FunSuite

class RuleBasedTokenizerTest extends FunSuite {

  test("testProcess") {
    val doc = new Document("This is a test string \n 4/12 8-9pm at 5pm est/6 pm est. Mon-Fri")
    val tokenizer = new RuleBasedTokenizer()
    tokenizer.process(doc)
    val expected = List(("This",0,4),("is",5,7),("a",8,9),("test",10,14),("string",15,21),("\n",22,23),
      ("4/12",24,28),("8-9pm",29,34),("at",35,37),("5pm",38,41),("est",42,45),("/",45,46),("6",46,47),
      ("pm",48,50),("est",51,54),(".",54,55),("Mon-Fri",56,63))
    val generated = doc.tokens.map(x => (x.string, x.stringStart, x.stringEnd))
    assert(expected == generated)

  }

}
