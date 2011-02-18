package com.futurenotfound.visualtron

import scala.collection.JavaConversions._
import scala.collection.Map
import java.util.{Map => JavaMap}
import scala.swing.event.Event
import reflect.ClassManifest
import swing._

case class ComponentsLoadedEvent(val components: Seq[ComponentBundle]) extends Event
case class ComponentBundle(name: String, component: Component)

class ResultHandler extends Publisher {
  private[this] val handlers: List[HandlerType] = List(
    new ListViewHandlerType(),
    new TableHandlerType(),
    new TextHandlerType()
  )
  def handle(result: Any): Unit = {
    val moreSpecificResult = result match {
      case iterable: java.lang.Iterable[_] => asScalaIterable(iterable).toList
      case javaMap: JavaMap[_,_] => asScalaMap(javaMap)
      case _ => result
    }
    val components: Seq[ComponentBundle] = identifyHandlers(moreSpecificResult).collect{case Some(bundle) => bundle}
    publish(ComponentsLoadedEvent(components))
  }

  private[this] def identifyHandlers(result: Any): Seq[Option[ComponentBundle]] = {
    handlers.map(handler => handler.getComponent(result))
  }
}

trait HandlerType {
  def getComponent(result: Any): Option[ComponentBundle]
}

case class ListViewHandlerType() extends HandlerType {
  def getComponent(result: Any): Option[ComponentBundle] = {
    result match {
      case seq: Seq[_] => Some(ComponentBundle("List", new ListView(seq)))
      case _ => None
    }
  }
}

case class TableHandlerType() extends HandlerType {
  def getComponent(result: Any): Option[ComponentBundle] = {
    result match {
      case map: Map[_, _] => {
        val columnTitles = List("Key", "Value")
        val data: Array[Array[Any]] = map.map(entry => Array(entry._1, entry._2)).toArray
        val table = new Table(data, columnTitles)
        Some(ComponentBundle("Table", table))
      }
      case _ => None
    }
  }
}

case class TextHandlerType() extends HandlerType {
  def getComponent(result: Any): Option[ComponentBundle] = {
    Some(ComponentBundle("Text", new TextArea(result.toString())))
  }
}