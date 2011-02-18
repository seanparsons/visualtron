package com.futurenotfound.visualtron

import scala.io._
import javax.swing._
import javax.script. {ScriptEngine, ScriptEngineManager}
import java.util.concurrent. {Executors, ExecutorService}
import scala.collection.immutable._
import scala.swing.event.Event
import swing.{Component, Publisher}

case class ScriptLoadedEvent(val script: String) extends Event
case class LoadingURLEvent(val url: String) extends Event

class Loader(val resultHandler: ResultHandler) extends Publisher {
  private val executorService: ExecutorService = Executors.newSingleThreadExecutor
  private var lastLoaded: String = ""

  def loadText(groovyText: String): Unit = {
    println(groovyText)
    refreshContent(groovyText, null)
  }

  def load(url: String): Unit = {
    loadText(getText(url))
  }

  private def refreshContent(groovyText: String, url: String): Unit = {
    SwingUtilities.invokeLater(new Runnable {
      def run: Unit = {
        lastLoaded = groovyText
        publish(new ScriptLoadedEvent(lastLoaded))
        val result = evaluateScript(groovyText)
        resultHandler.handle(result)
      }
    })
  }

  private def getTextAsSource(url: String) = Source.fromURL(url)

  def getText(url: String): String = getTextAsSource(url).mkString

  private def readFromURL(url: String): String = {
    publish(new LoadingURLEvent(url))
    getText(url)
  }

  private def evaluateScript(groovyText: String): Any = {
    val factory = new ScriptEngineManager()
    val engine: ScriptEngine = factory.getEngineByName("groovy")
    engine.put("loader", this)
    return engine.eval(groovyText)
  }
}