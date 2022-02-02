package me.ore.swing.ext.tabs.dnd

import java.awt.datatransfer.DataFlavor


/**
 * Data flavor for class [OreTabTransferData]
 */
object OreTabTransferDataFlavor: DataFlavor(OreTabTransferData::class.java, OreTabTransferData::class.java.simpleName)
