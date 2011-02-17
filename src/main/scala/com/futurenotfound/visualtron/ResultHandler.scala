package com.futurenotfound.visualtron

import scala.swing.{ListView, Component, Publisher, Table}
import scala.collection.JavaConversions._
import java.util.{Map => JavaMap}

class ResultHandler extends Publisher {
  def handle(result: Any): Unit = {
    val component: Option[Component] = identifyHandler(result)
    println(component)
    if (component.isDefined) publish(new ComponentLoadedEvent(component.get))
  }

  private[this] def identifyHandler(result: Any): Option[Component] = {
    result match {
      case seq: Seq[_] => Some(new ListView(seq))
      case map: Map[_, _] => {
        val columnTitles = map.keySet.toList
        Some(new Table(Array(columnTitles.map(key => map(key)).toArray), columnTitles))
      }
      case iterable: java.lang.Iterable[_] => identifyHandler(asScalaIterable(iterable).toList)
      case javaMap: JavaMap => identifyHandler(asScalaMap(javaMap))
      case _ => {
        println(result.asInstanceOf[AnyRef].getClass())
        None
      }
    }
  }
}