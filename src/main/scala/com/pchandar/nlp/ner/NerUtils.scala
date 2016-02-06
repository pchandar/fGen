package com.pchandar.nlp.ner

object NerUtils{

  /** Given a sequence of strings describing labels NOT in IOB format, such as O A B C,
      (where O means OTHER, A, B and C are target types)
      return a sequence of tuples indicating span start, length and label suffix, such as (3, 2, "B"). */
  def multiclassBoundaries(labels:Seq[String]): Seq[(Int,Int,String)] = {
    val result = new scala.collection.mutable.ArrayBuffer[(Int,Int,String)]

    var start = -1
    var prevType = "O"
    for (i <- labels.indices) {
      val atBoundary = labels(i) != prevType
      if (start >= 0 && atBoundary) { result.+=((start, i-start, labels(i-1))); start = -1 }
      if (labels(i) != "O" && atBoundary){
        start = i
        if (i == labels.length-1)
          result.+=((start, 1, labels(i)))
      }
      prevType = labels(i)
    }

    result

  }

  /** Given a sequence of strings describing labels in IOB format, such as O I-PER I-LOC B-LOC I-LOC O I-ORG,
      (where I, B prefixes are separated by a dash from the type suffix)
      return a sequence of tuples indicating span start, length and label suffix, such as (3, 2, "LOC"). */
  def iobBoundaries(labels:Seq[String]): Seq[(Int,Int,String)] = {
    val result = new scala.collection.mutable.ArrayBuffer[(Int,Int,String)]
    val strings = labels.map(_.split('-'))
    val iobs = strings.map(_.apply(0))
    val types = strings.map(a => if (a.length > 1) a(1) else "")
    var start = -1; var prevType = ""
    for (i <- labels.indices) {
      val atBoundary = types(i) != prevType || iobs(i) == "B"
      if (start >= 0 && atBoundary) { result.+=((start, i-start, types(i-1))); start = -1 }
      if (types(i) != "" && atBoundary){
        start = i
        if (i == labels.length-1)
          result.+=((start, 1, types(i)))
      }
      prevType = types(i)
    }
    result
  }

  def bilouBoundaries(labels:Seq[String]): Seq[(Int,Int,String)] = {
    val result = new scala.collection.mutable.ArrayBuffer[(Int,Int,String)]
    val strings = labels.map(_.split('-'))
    val bilous = strings.map(_.apply(0))
    val types = strings.map(a => if (a.length > 1) a(1) else "")
    var start = -1; var prevType = ""
    for (i <- labels.indices) {
      val atBoundary = types(i) != prevType || bilous(i) == "B" || bilous(i) == "U"
      if (bilous(i) == "U") result.+=((i, 1, types(i)))
      else if (start >= 0 && atBoundary) { result.+=((start, i-start, types(i-1))); start = -1 }
      if (types(i) != "" && atBoundary){
        start = i
        if (i == labels.length-1)
          result.+=((start, 1, types(i)))
      }
      prevType = types(i)
    }
    result
  }

  /** Convenience alias for @see com.xdotai.nlp.iobBoundaries */
  def bioBoundaries(labels:Seq[String]): Seq[(Int,Int,String)] = iobBoundaries(labels)

}

