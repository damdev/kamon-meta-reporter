package io.kamon.meta.reporter

import java.io.IOException

import com.grack.nanojson.{JsonStringWriter, JsonWriter}
import com.typesafe.config.{Config, ConfigUtil}
import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD._
import io.kamon.meta.reporter.MetaReporter.{Configuration, EmbeddedHttpServer}
import kamon.Kamon
import kamon.module.Module

import scala.collection.JavaConverters._

class MetaReporter extends Module {
  import MetaReporter.Configuration.readConfiguration

  override def start(): Unit = try {
    val config = readConfiguration(Kamon.config())
    if (config.startEmbeddedServer)
      startEmbeddedServer(config)
  } catch {
    case e: IOException =>
      e.printStackTrace()
  }

  override def stop(): Unit = {
    stopEmbeddedServer()
  }

  private var embeddedHttpServer: Option[EmbeddedHttpServer] = None

  private def startEmbeddedServer(config: Configuration): Unit = {
    val server = new EmbeddedHttpServer(config.embeddedServerHostname, config.embeddedServerPort)
    server.start()
    embeddedHttpServer = Some(server)
  }

  private def stopEmbeddedServer(): Unit = {
    embeddedHttpServer.foreach(_.stop)
  }

  override def reconfigure(newConfig: Config): Unit = {
    stopEmbeddedServer()
    val config = readConfiguration(newConfig)
    if (config.startEmbeddedServer)
      startEmbeddedServer(config)
  }

}

object MetaReporter {

  def scrapeData: String = {
    val modules = JsonWriter.string.`object`.array("modules")
    val modulesJson = KanelaInfoProvider.getKanelaModulesInfo.asScala.foldLeft(modules) {
      (mods: JsonStringWriter, m: (String, java.util.Map[String, java.util.List[String]])) =>
        addModuleJson(mods.`object`, m)
    }.end()
    KanelaInfoProvider.getKanelaErrors.asScala.foldLeft(modulesJson.array("errors")) {
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

  private class EmbeddedHttpServer(val hostname: String, val port: Int) extends NanoHTTPD(hostname, port) {
    override def serve(session: NanoHTTPD.IHTTPSession): NanoHTTPD.Response = newFixedLengthResponse(Response.Status.OK, "application/json", scrapeData)
  }

  case class Configuration(startEmbeddedServer: Boolean, embeddedServerHostname: String, embeddedServerPort: Int)

  object Configuration {

    def readConfiguration(config: Config): MetaReporter.Configuration = {
      val metaReporterConfig = config.getConfig("kamon.meta-reporter")

      MetaReporter.Configuration(
        startEmbeddedServer = metaReporterConfig.getBoolean("start-embedded-http-server"),
        embeddedServerHostname = metaReporterConfig.getString("embedded-server.hostname"),
        embeddedServerPort = metaReporterConfig.getInt("embedded-server.port")
      )
    }

  }
}
