package com.futurenotfound.visualtron

import scala.collection.JavaConversions._
import scala.collection.Map
import java.util.{Map => JavaMap}
import scala.swing.event.Event
import swing._
import org.jfree.data.general.DefaultPieDataset
import org.jfree.chart.plot.PiePlot3D
import org.jfree.chart.{ChartPanel, ChartFactory}

case class ComponentsLoadedEvent(val components: Seq[ComponentBundle]) extends Event
case class ComponentBundle(name: String, component: Component)
case class TextToDoubleMap(map: Map[_, Double])
case class DoubleToDoubleMap(map: Map[Double, Double])

class ResultHandler extends Publisher {
  private[this] val handlers: List[HandlerType] = List(
    new ListViewHandlerType(),
    new TableHandlerType(),
    new TextHandlerType(),
    new PieChartHandlerType()
  )
  def handle(result: Any): Unit = {
    val moreSpecificResult = improveType(result)
    val components: Seq[ComponentBundle] = identifyHandlers(moreSpecificResult).collect{case Some(bundle) => bundle}
    publish(ComponentsLoadedEvent(components))
  }

  private[this] def identifyHandlers(result: Any): Seq[Option[ComponentBundle]] = {
    handlers.map(handler => handler.getComponent(result))
  }

  private[this] def improveType(result: Any): Any = {
    val improvedType = result match {
      case iterable: java.lang.Iterable[_] => asScalaIterable(iterable).toList
      case javaMap: JavaMap[_,_] => asScalaMap(javaMap)
      case map: Map[_,_] => improveMap(map)
      case _ => result
    }
    if (improvedType.asInstanceOf[AnyRef].getClass() == result.asInstanceOf[AnyRef].getClass()) {
      return improvedType
    } else {
      return improveType(improvedType)
    }
  }

  private[this] def improveMap(map: Map[_,_]): Any = {
    map match {
      case map => {
        val numberValues: Iterable[Double] = map.values.collect{
          case int: Int => int2double(int)
          case long: Long => long2double(long)
          case double: Double => double
        }
        if (numberValues.size == map.size) {
          TextToDoubleMap(map.keys.zip(numberValues).toMap)
        }
      }
    }
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

case class PieChartHandlerType() extends HandlerType {
  def getComponent(result: Any): Option[ComponentBundle] = {
    result match {
      case map: TextToDoubleMap => {
        val dataSet = new DefaultPieDataset();
        map.map.entrySet.foreach{entry =>
          dataSet.setValue(entry.getKey().toString(), entry.getValue())
        }
        val chart = ChartFactory.createPieChart3D(null, dataSet, true, true, false)
        val plot = chart.getPlot().asInstanceOf[PiePlot3D]
        plot.setForegroundAlpha(0.60f)
        plot.setInteriorGap(0.33)
        val chartPanel = new ChartPanel(chart)
        Some(ComponentBundle("Pie Chart", Component.wrap(chartPanel)))
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