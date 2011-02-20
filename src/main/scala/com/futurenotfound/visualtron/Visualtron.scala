package com.futurenotfound.visualtron

import javax.swing.UIManager
import com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel
import scala.swing._
import scala.swing.event.Key.Enter
import scala.swing.event.Key.Modifier.Control
import scala.swing.event.KeyPressed
import java.awt.Color
import java.awt.event.{KeyEvent, KeyListener}

object Visualtron extends SimpleSwingApplication {
  UIManager.setLookAndFeel(new NimbusLookAndFeel());

  private val resultHandler = new ResultHandler()
  private val loader = new Loader(resultHandler)
  private val startingHeight = 600

  private val editorArea = new TextArea {}
  private val editorScrollPane = new ScrollPane {
    contents = editorArea
  }
  private val visualsScrollPane = new ScrollPane {
  }
  private val splitPane = new SplitPane {
    leftComponent = editorScrollPane
    rightComponent = visualsScrollPane
    dividerLocation = startingHeight / 2
  }
  private val borderPanel = new BorderPanel {
    add(splitPane, BorderPanel.Position.Center)
  }

  listenTo(loader, resultHandler, editorArea.keys)
  reactions += {
    case KeyPressed(`editorArea`, Enter, Control, _) => {
      loader.loadText(editorArea.text)
    }
    case componentsLoaded: ComponentsLoadedEvent => {
      val originalDividerLocation = splitPane.dividerLocation
      splitPane.rightComponent = new TabbedPane {
        pages ++= componentsLoaded.components.map{bundle =>
          val innerScrollPane = new ScrollPane(new GridBagPanel{
            val constraints = new Constraints
            constraints.fill = GridBagPanel.Fill.Both
            constraints.gridx = 0
            constraints.gridy = 0
            constraints.gridwidth = 1
            constraints.gridheight = 1
            constraints.weightx = 1
            constraints.weighty = 1
            layout(bundle.component) = constraints
          })
          new TabbedPane.Page(bundle.name, innerScrollPane)
        }
      }
      splitPane.dividerLocation = originalDividerLocation
    }
  }

  def top = new MainFrame {
    title = "Visualtron"
    contents = borderPanel
    size = new Dimension(800, startingHeight)
  }
}