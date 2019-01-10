package io.kamon.meta.reporter

trait KanelaInfoProvider
object KanelaInfoProvider {
  def getRecorded: java.util.Map[String, java.util.Map[String, java.util.List[String]]] = java.util.Collections.EMPTY_MAP.asInstanceOf[java.util.Map[String, java.util.Map[String, java.util.List[String]]]]

  def getErrors: java.util.Map[String, java.util.List[Throwable]] = java.util.Collections.EMPTY_MAP.asInstanceOf[java.util.Map[String, java.util.List[Throwable]]]
}
