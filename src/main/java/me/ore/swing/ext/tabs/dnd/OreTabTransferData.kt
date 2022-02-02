package me.ore.swing.ext.tabs.dnd

import me.ore.swing.ext.tabs.OreTab
import me.ore.swing.ext.tabs.OreTabPane
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException


/**
 * Information about dragged tab
 *
 * @param pane Tab pane
 * @param tab Tab
 * @param index Index of [tab] in [pane]
 */
data class OreTabTransferData(
    val pane: OreTabPane,
    val tab: OreTab,
    val index: Int
): Transferable {
    companion object {
        /**
         * Array of data flavors applicable to objects of class [OreTabTransferData]
         */
        private val FLAVORS: Array<DataFlavor> = arrayOf(OreTabTransferDataFlavor)
    }

    override fun getTransferDataFlavors(): Array<DataFlavor> = FLAVORS

    override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean = (flavor == OreTabTransferDataFlavor)

    override fun getTransferData(flavor: DataFlavor?): Any {
        if (flavor != OreTabTransferDataFlavor)
            throw UnsupportedFlavorException(flavor)
        return this
    }
}
