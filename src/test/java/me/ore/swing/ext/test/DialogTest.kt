package me.ore.swing.ext.test

import me.ore.swing.ext.test.util.OreSwingExtTest
import me.ore.swing.ext.util.OreGraphics2DConfig
import me.ore.swing.ext.util.OreSwingShapeInfo
import java.awt.*
import java.awt.geom.Rectangle2D
import java.io.File
import javax.swing.*


object DialogTest {
    private val LAF = OreSwingExtTest.LafChoice.SYSTEM

    private val RENDERING_HINTS = mapOf(
        RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
        RenderingHints.KEY_FRACTIONALMETRICS to RenderingHints.VALUE_FRACTIONALMETRICS_ON
    )

    @JvmStatic
    fun main(args: Array<String>) {
        EventQueue.invokeLater {
            OreSwingExtTest.applyLaf(LAF)

            val frame = JFrame("FS Dialog test").also frame@ { frame ->
                frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
                frame.size = Dimension(800, 600)
                frame.isLocationByPlatform = true
                frame.layout = GridBagLayout()
            }

            val messageButton = JButton("Message").also messageButton@ { messageButton ->
                frame.add(messageButton, GridBagConstraints().apply { this.fill = GridBagConstraints.CENTER })
            }

            val jFileButton = JButton("J File").also jFileButton@ { jFileButton ->
                frame.add(jFileButton, GridBagConstraints().apply { this.fill = GridBagConstraints.CENTER })
            }

            messageButton.addActionListener {
                val image = OreSwingShapeInfo(
                    Rectangle2D.Double(0.0, 0.0, 24.0, 24.0),
                    "M11 6C11 5.7 11.1 4.6 11.8 3.8L10 2.4L8.2 3.9C8.9 4.6 9 5.7 9 6C2.7 6.4 0 11 0 11L7 18C7 18 7.7 16.7 9 16.2V18.3C8.4 18.6 8 19.3 8 20C8 21.11 8.9 22 10 22S12 21.11 12 20C12 19.3 11.6 18.6 11 18.3V16.2C12.3 16.7 13 18 13 18L20 11C20 11 17.3 6.5 11 6M9 14.1C8.2 14.3 7.5 14.6 6.9 15.1L2.7 10.9C3.8 9.8 5.8 8.3 9 8.1V14.1M13.1 15.1C12.5 14.7 11.8 14.3 11 14.1V8.1C14.2 8.4 16.2 9.8 17.3 10.9L13.1 15.1M16 1.3L15.3 3.2C14.6 2.9 13.5 2.9 12.7 3.2L12 1.3C13.2 .9 14.8 .9 16 1.3M19 6H17C17 6 17 4.7 16.2 3.9L17.7 2.6C19 4 19 5.9 19 6M2.2 2.6L3.7 3.9C3 4.7 3 6 3 6H1C1 5.9 1 4 2.2 2.6M8 1.3L7.3 3.2C6.6 2.9 5.5 2.9 4.7 3.2L4 1.3C5.2 .9 6.8 .9 8 1.3M22 12V7H24V13H22M22 17H24V15H22"
                )
                    .scale(36.0 / 24)
                    .toImage(OreGraphics2DConfig(color = Color(165, 28, 28), renderingHints = RENDERING_HINTS))

                JOptionPane.showMessageDialog(frame, JLabel("Message"), "Message", JOptionPane.INFORMATION_MESSAGE, ImageIcon(image))
            }

            jFileButton.addActionListener {
                EventQueue.invokeLater {
                    val image = OreSwingShapeInfo(
                        Rectangle2D.Double(0.0, 0.0, 24.0, 24.0),
                        "M19,3H5A2,2 0 0,0 3,5V19A2,2 0 0,0 5,21H19A2,2 0 0,0 21,19V5A2,2 0 0,0 19,3M19,19H5V5H19V19M13.94,10.06C14.57,10.7 14.92,11.54 14.92,12.44C14.92,13.34 14.57,14.18 13.94,14.81L11.73,17C11.08,17.67 10.22,18 9.36,18C8.5,18 7.64,17.67 7,17C5.67,15.71 5.67,13.58 7,12.26L8.35,10.9L8.34,11.5C8.33,12 8.41,12.5 8.57,12.94L8.62,13.09L8.22,13.5C7.91,13.8 7.74,14.21 7.74,14.64C7.74,15.07 7.91,15.47 8.22,15.78C8.83,16.4 9.89,16.4 10.5,15.78L12.7,13.59C13,13.28 13.18,12.87 13.18,12.44C13.18,12 13,11.61 12.7,11.3C12.53,11.14 12.44,10.92 12.44,10.68C12.44,10.45 12.53,10.23 12.7,10.06C13.03,9.73 13.61,9.74 13.94,10.06M18,9.36C18,10.26 17.65,11.1 17,11.74L15.66,13.1V12.5C15.67,12 15.59,11.5 15.43,11.06L15.38,10.92L15.78,10.5C16.09,10.2 16.26,9.79 16.26,9.36C16.26,8.93 16.09,8.53 15.78,8.22C15.17,7.6 14.1,7.61 13.5,8.22L11.3,10.42C11,10.72 10.82,11.13 10.82,11.56C10.82,12 11,12.39 11.3,12.7C11.47,12.86 11.56,13.08 11.56,13.32C11.56,13.56 11.47,13.78 11.3,13.94C11.13,14.11 10.91,14.19 10.68,14.19C10.46,14.19 10.23,14.11 10.06,13.94C8.75,12.63 8.75,10.5 10.06,9.19L12.27,7C13.58,5.67 15.71,5.68 17,7C17.65,7.62 18,8.46 18,9.36Z"
                    )
                        .scale(16.0 / 24)
                        .toImage(OreGraphics2DConfig(color = Color(165, 28, 28), renderingHints = RENDERING_HINTS))

                    val chooser = JFileChooser()

                    UIManager.put("FileChooser.acceptAllFileFilterText", "Yea, ALL!")
                    UIManager.put("FileChooser.openButtonText", "Open this")
                    UIManager.put("FileChooser.saveButtonText", "Save that")
                    UIManager.put("FileChooser.upFolderIcon", ImageIcon(image))
                    SwingUtilities.updateComponentTreeUI(chooser)
                    chooser.currentDirectory = File(System.getProperty("user.home"))
                    chooser.dialogType = JFileChooser.SAVE_DIALOG
                    chooser.isMultiSelectionEnabled = true
                    chooser.showOpenDialog(frame)
                    println((chooser.selectedFiles ?: emptyArray()).plus(chooser.selectedFile.let { if (it == null) { emptyArray() } else { arrayOf(it) } }).toList())
                }
            }

            frame.isVisible = true
        }
    }
}
