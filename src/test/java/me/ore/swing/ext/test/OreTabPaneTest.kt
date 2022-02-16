package me.ore.swing.ext.test

import me.ore.swing.ext.OreDnDImageRender
import me.ore.swing.ext.tabs.*
import me.ore.swing.ext.tabs.dnd.OreTabTransferHandler
import me.ore.swing.ext.test.util.OreSwingExtTest
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.*
import kotlin.random.Random


object OreTabPaneTest {
    private val LAF = OreSwingExtTest.LafChoice.SYSTEM

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {
            OreSwingExtTest.applyLaf(LAF)
            UIManager.put(OreTabPaneUI.UI_KEY__DROP_LOCATION__COLOR, Color(255, 0, 0, 150))

            OreDnDImageRender.init()

            val frame = JFrame("Test")
            frame.size = Dimension(800, 600)
            frame.isLocationByPlatform = true
            frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
            frame.layout = BorderLayout()
            frame.isVisible = true

            val toolPane = JPanel(FlowLayout(FlowLayout.LEFT))
            frame.add(toolPane, BorderLayout.NORTH)

            val addButton = JButton("Add")
            toolPane.add(addButton)

            val setTitleButton = JButton("Title")
            toolPane.add(setTitleButton)
            setTitleButton.isEnabled = false

            val setContentButton = JButton("Content")
            toolPane.add(setContentButton)
            setContentButton.isEnabled = false

            val changeCloseableButton = JButton("<×>")
            toolPane.add(changeCloseableButton)
            changeCloseableButton.isEnabled = false

            val closeButton = JButton("×")
            toolPane.add(closeButton)
            closeButton.isEnabled = false

            val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true)
            frame.add(splitPane, BorderLayout.CENTER)

            val leftScrollPane = JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            splitPane.leftComponent = leftScrollPane

            val leftPane = OreTabPane()
            leftScrollPane.viewport.view = leftPane
            leftPane.model.addListener(object: OreTabModelAdapter() {
                private fun setButtonsEnabled(model: OreTabModel) {
                    model.tabs.isNotEmpty().let {
                        setTitleButton.isEnabled = it
                        setContentButton.isEnabled = it
                        changeCloseableButton.isEnabled = it
                        closeButton.isEnabled = it
                    }
                }

                override fun tabAdded(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
                    this.setButtonsEnabled(model)
                }

                override fun tabRemoved(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
                    this.setButtonsEnabled(model)
                }
            })

            val rightScrollPane = JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
            splitPane.rightComponent = rightScrollPane

            val rightPane = OreTabPane()
            rightScrollPane.viewport.view = rightPane
            rightPane.addPropertyChangeListener { event ->
                println(event)
            }
            rightPane.model.addListener(object: OreTabModelListener {
                override fun tabAdded(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
                    println("tabAdded - $tabAndIndex")
                }

                override fun tabRemoved(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
                    println("tabRemoved - $tabAndIndex")
                }

                override fun tabMoved(model: OreTabModel, moveResult: OreTabMoveResult) {
                    println("tabMoved - $moveResult")
                }

                override fun tabSelectionAppear(model: OreTabModel, newSelection: OreTabAndIndex) {
                    println("tabSelectionAppear - $newSelection")
                }

                override fun tabSelectionChanged(model: OreTabModel, oldSelection: OreTabAndIndex, newSelection: OreTabAndIndex) {
                    println("tabSelectionChanged - $oldSelection -> $newSelection")
                    //RuntimeException("tabSelectionChanged").printStackTrace(System.out)
                }

                override fun tabSelectionLost(model: OreTabModel, oldSelection: OreTabAndIndex) {
                    println("tabSelectionLost - $oldSelection")
                }

            })
            rightPane.tabPlacement = OreTabPane.TabPlacement.LEFT
            rightPane.tabLayoutPolicy = OreTabPane.TabLayoutPolicy.SCROLL

            leftPane.transferHandler = OreTabTransferHandler()
            leftPane.useTransferHandlerOnTabDragged = true
            leftPane.useDndImageRendererOnTabDragged = true

            rightPane.transferHandler = OreTabTransferHandler()
            rightPane.useTransferHandlerOnTabDragged = true
            rightPane.useDndImageRendererOnTabDragged = true

            frame.addWindowListener(object: WindowAdapter() {
                override fun windowOpened(e: WindowEvent?) {
                    frame.removeWindowListener(this)
                    EventQueue.invokeLater {
                        splitPane.setDividerLocation(0.33)
                    }
                }
            })

            val randomTab = {
                val index = Random(System.currentTimeMillis()).nextInt(leftPane.model.tabs.size)
                val tab = leftPane.model.tabs[index]
                tab
            }

            addButton.addActionListener {
                val tab = OreTab(closeable = true)
                leftPane.model.addTab(tab)
            }

            setTitleButton.addActionListener {
                val tab = randomTab()
                tab.title = JLabel("JLabel - ${Random(System.currentTimeMillis()).nextInt(900) + 100}")
            }

            setContentButton.addActionListener {
                val tab = randomTab()
                val random = Random(System.currentTimeMillis())
                val color = Color(
                    random.nextInt(256),
                    random.nextInt(256),
                    random.nextInt(256)
                )

                tab.content = JPanel().also { panel ->
                    panel.layout = BorderLayout()
                    panel.background = color
                    (tab.title as? JLabel)?.let {
                        panel.add(JLabel(it.text), BorderLayout.CENTER)
                    }
                }
            }

            changeCloseableButton.addActionListener {
                val tab = randomTab()
                tab.closeable = !tab.closeable
            }

            closeButton.addActionListener {
                val tab = leftPane.model.tabs.last()
                leftPane.model.removeTab(tab)
            }
        }
    }
}
