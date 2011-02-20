package com.futurenotfound.visualtron

import scala.collection.JavaConversions._
import scala.collection.Map
import java.util.{Map => JavaMap}
import scala.swing.event.Event
import swing._
import org.jfree.chart.{ChartPanel, ChartFactory}
import org.jfree.data.general.{DefaultKeyedValues2DDataset, DefaultPieDataset}
import org.jfree.data.xy.{XYSeries, XYSeriesCollection}
import org.jfree.chart.plot.{PlotOrientation, PiePlot3D}
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import java.awt.Color

case class ComponentsLoadedEvent(val components: Seq[ComponentBundle]) extends Event
case class ComponentBundle(name: String, component: Component)
case class TextToDoubleMap(map: Map[_, Double])
case class DoubleToDoubleMap(xTitle: String, yTitle: String, map: Map[Double, Double])

class ResultHandler extends Publisher {
  private[this] val handlers: List[HandlerType] = List(
    new LineChartHandlerType(),
    new PieChartHandlerType(),
    new ListViewHandlerType(),
    new TableHandlerType(),
    new TextHandlerType()
  )
  def handle(result: Any): Unit = {
    val moreSpecificResult = improveType(result)
    println("moreSpecificResult = " + moreSpecificResult)
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
      case map: TextToDoubleMap => improveDoubleMap(map)
      case _ => result
    }
    if (improvedType.asInstanceOf[AnyRef].getClass() == result.asInstanceOf[AnyRef].getClass()) {
      return improvedType
    } else {
      return improveType(improvedType)
    }
  }

  private[this] def improveDoubleMap(map: TextToDoubleMap): Any = {
    val numberKeys = collectNumbers(map.map.keys)
    if (numberKeys.size == map.map.size) {
      DoubleToDoubleMap(null, null, numberKeys.zip(map.map.values).toMap)
    }
    else map
  }

  private[this] def collectNumbers(possibleNumbers: Iterable[_]): Iterable[Double] = {
    return possibleNumbers.collect {
      case int: Int => int2double(int)
      case long: Long => long2double(long)
      case double: Double => double
    }
  }

  private[this] def improveMap(map: Map[_,_]): Any = {
    val numberValues: Iterable[Double] = collectNumbers(map.values)
    if (numberValues.size == map.size) {
      TextToDoubleMap(map.keys.zip(numberValues).toMap)
    }
    else map
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
      case map: TextToDoubleMap => getComponent(map.map)
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

case class LineChartHandlerType() extends HandlerType {
  def getComponent(result: Any): Option[ComponentBundle] = {
    result match {
      case DoubleToDoubleMap(xTitle, yTitle, map) => {
        val dataSet = new XYSeriesCollection()
        val series = new XYSeries("Values")
        map.entrySet.foreach{entry =>
          series.add(entry.getKey(), entry.getValue())
        }
        dataSet.addSeries(series)
        val chart = ChartFactory.createXYLineChart(null, xTitle, yTitle, dataSet, PlotOrientation.VERTICAL, false, false, false)
        val plot = chart.getXYPlot()
        val renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(1, true);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);
        val chartPanel = new ChartPanel(chart)
        Some(ComponentBundle("Line Chart", Component.wrap(chartPanel)))
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
        //plot.setInteriorGap(0.33)
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