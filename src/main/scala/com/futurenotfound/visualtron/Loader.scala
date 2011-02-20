package com.futurenotfound.visualtron

import scala.io._
import java.util.concurrent. {Executors, ExecutorService}
import scala.collection.immutable._
import scala.swing.event.Event
import java.io.{PrintWriter, StringWriter}
import scala.swing.{TextArea, Publisher}
import java.util.concurrent.atomic.AtomicReference
import tools.nsc.{Settings, Interpreter}

case class LoadingURLEvent(val url: String) extends Event

class Loader(val resultHandler: ResultHandler) extends Publisher {
  private val executorService: ExecutorService = Executors.newSingleThreadExecutor

  def loadText(script: String): Unit = {
    refreshContent(script, null)
  }

  def load(url: String): Unit = {
    loadText(getText(url))
  }

  private def refreshContent(script: String, url: String): Unit = {
    executorService.execute(new Runnable {
      def run: Unit = {
        try {
          val result = evaluateScript(script)
          resultHandler.handle(result)
        }
        catch {
          case throwable: Throwable => {
            val stringWriter = new StringWriter()
            val printWriter = new PrintWriter(stringWriter)
            throwable.printStackTrace(printWriter)
            publish(ComponentsLoadedEvent(List(ComponentBundle("Error", new TextArea(stringWriter.toString())))))
          }
        }
      }
    })
  }

  private def getTextAsSource(url: String) = Source.fromURL(url)

  def getText(url: String): String = getTextAsSource(url).mkString

  private def readFromURL(url: String): String = {
    publish(new LoadingURLEvent(url))
    getText(url)
  }

  private def evaluateScript(script: String): Any = {
    val settings = new Settings()
    settings.usejavacp.value = true
    settings.classpath.value = System.getProperty("java.class.path")
    println(settings.classpath.value)
    val interpreter = new Interpreter(settings)
    interpreter.setContextClassLoader()
    val atomicReference = new AtomicReference()
    interpreter.bind("result", atomicReference.getClass().getCanonicalName() + "[Any]", atomicReference)
    script.split("\r+\n+").map(_.trim).filter(_.length > 0).foreach{line =>
      interpreter.interpret(line)
    }
    return atomicReference.get()
  }
}