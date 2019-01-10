package io.kamon.meta.reporter

import java.io.IOException
import java.util.Optional

import com.grack.nanojson.{JsonStringWriter, JsonWriter}
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD._

import scala.collection.JavaConverters._

object MetaReporter {

  def start() = try
    startEmbeddedServer()
  catch {
    case e: IOException =>
      e.printStackTrace()
  }

  private var embeddedHttpServer: Option[EmbeddedHttpServer] = None

  def scrapeData: String = {
    val modules = JsonWriter.string.`object`.array("modules")
    val modulesJson = KanelaInfoProvider.getRecorded.asScala.foldLeft(modules) {
      (mods: JsonStringWriter, m: (String, java.util.Map[String, java.util.List[String]])) =>
        addModuleJson(mods.`object`, m)
    }.end()
    KanelaInfoProvider.getErrors.asScala.foldLeft(modulesJson.array("errors")) {
      (errs: JsonStringWriter, e: (String, java.util.List[Throwable])) =>
        e._2.asScala.foldLeft(errs.`object`.value("type", e._1).array) {
          (tws: JsonStringWriter, tw: Throwable) => tws.value(tw.getMessage).end
        }
    }.end.end.done()
  }

  private def addModuleJson(`object`: JsonStringWriter, module: (String, java.util.Map[String, java.util.List[String]])): JsonStringWriter = {
    val instrumentations = `object`.value("name", module._1).array("instrumentations")
    module._2.asScala.foldLeft(instrumentations){(inst: JsonStringWriter, i: (String, java.util.List[String])) => addInstrumentationJson(inst.`object`, i)}.end.end
  }

  private def addInstrumentationJson(`object`: JsonStringWriter, instrumentation: (String, java.util.List[String])): JsonStringWriter = {
    val types = `object`.value("name", instrumentation._1).array("types")
    instrumentation._2.asScala.foldLeft(types){(ts: JsonStringWriter, t: String) => ts.value(t)}.end.end
  }

  def stop(): Unit = {
    stopEmbeddedServer()
  }

  private class EmbeddedHttpServer(val hostname: String, val port: Int) extends NanoHTTPD(hostname, port) {
    override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response = newFixedLengthResponse(Response.Status.OK, "application/json", scrapeData)
  }

  @throws[IOException]
  private def startEmbeddedServer(): Unit = {
    val server = new EmbeddedHttpServer("0.0.0.0", 9999)
    server.start()
    embeddedHttpServer = Some(server)
  }

  private def stopEmbeddedServer(): Unit = {
    embeddedHttpServer.foreach(_.stop)
  }
}