package com.futurenotfound.visualtron

import scala.swing.{ListView, Component, Publisher}
import scala.collection.JavaConversions._

class ResultHandler extends Publisher {
  def handle(result: Any): Unit = {
    val component: Option[Component] = result match {
      case seq: Seq[_] => Some(new ListView(seq))
      case iterable: java.lang.Iterable[_] => Some(new ListView(asScalaIterable(iterable).toList))
      case _ => {
        println(result.asInstanceOf[AnyRef].getClass())
        None
      }
    }
    println(component)
    if (component.isDefined) publish(new ComponentLoadedEvent(component.get))
  }
}