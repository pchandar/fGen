package com.pchandar.features

object FeatureFunctionUtils {
  def toBatchFn[T, S](f: T => Iterable[S]): Seq[T] => Map[T, Iterable[S]] = {
    ts => ts.map(t => (t, f(t)))(scala.collection.breakOut)
  }
}
