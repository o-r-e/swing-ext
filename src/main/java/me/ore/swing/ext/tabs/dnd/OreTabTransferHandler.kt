package me.ore.swing.ext.tabs.dnd

import me.ore.swing.ext.tabs.OreTabAndIndex
import me.ore.swing.ext.tabs.OreTabPane
import java.awt.Point
import java.awt.datatransfer.Transferable
import java.awt.dnd.*
import javax.swing.JComponent
import javax.swing.TransferHandler


/**
 * Handler for dragging tabs between [OreTabPane]s
 */
@Suppress("LeakingThis")
open class OreTabTransferHandler: TransferHandler() {
    companion object {
        /**
         * Drawing information for drop location
         */
        protected var dropPaintData: OreTabDropPaintData? = null
            set(new) {
                if (field != new) {
                    val old = field
                    field = new

                    old?.targetPane?.clearDropLocation()
                    new?.targetPane?.paintDropLocation(new)
                }
            }

        init {
            DragSource.getDefaultDragSource().addDragSourceListener(object: DragSourceAdapter() {
                override fun dragExit(dse: DragSourceEvent?) {
                    super.dragExit(dse)
                    dropPaintData = null
                }

                override fun dragDropEnd(dsde: DragSourceDropEvent?) {
                    super.dragDropEnd(dsde)
                    dropPaintData = null
                }
            })
        }
    }


    // region Methods for export
    override fun getSourceActions(component: JComponent?): Int {
        val pane = (component as? OreTabPane)
        return if (pane == null) {
            NONE
        } else {
            MOVE
        }
    }

    override fun createTransferable(component: JComponent?): Transferable? {
        val pane = (component as? OreTabPane)
        val tabAndIndex = pane?.draggedTabAndIndex
        return tabAndIndex?.let { OreTabTransferData(pane, tabAndIndex.tab, tabAndIndex.index) }
    }

    override fun exportDone(source: JComponent?, data: Transferable?, action: Int) {
        super.exportDone(source, data, action)
        (source as? OreTabPane)?.endDragProcess()
    }
    // endregion


    // region Methods for import
    /**
     * Default handling for [TransferSupport][TransferHandler.TransferSupport]
     *
     * Aborts execution and returns [defaultResult] if:
     *
     * * [support] is `null`
     * * [support].[transferable][TransferHandler.TransferSupport.getTransferable] is `null`
     * * [support].[transferable][TransferHandler.TransferSupport.getTransferable] doesn't support [OreTabTransferDataFlavor]
     * * The result of calling
     * [support].[transferable][TransferHandler.TransferSupport.getTransferable].[getTransferable][TransferHandler.TransferSupport.getTransferable] ([OreTabTransferDataFlavor])
     * is not an object of class [OreTabTransferData]
     * * [support].[component][TransferHandler.TransferSupport.getComponent] is not object of class [OreTabPane]
     * * [support].[dropLocation][TransferHandler.TransferSupport.getDropLocation].[dropPoint][TransferHandler.DropLocation.getDropPoint]
     * is `null`
     *
     * If execution was not interrupted, calls [block] with the following parameters:
     *
     * * `support` - [support]
     * * `transferData` - information about dragged tab
     * * `targetPane` - the tab pane that the mouse cursor was within while dragging the tab
     * * `dropPoint` - mouse cursor coordinates within `targetPane`
     * * `targetTabAndIndex` - the tab in the `targetPane` under the mouse cursor and its index (if there is a tab under the cursor)
     *
     * @param support Object, which encapsulates all relevant details of drag and drop transfer
     * @param defaultResult Default result
     * @param block Executable block
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun <T> defaultImportRun(
        support: TransferSupport?,
        @Suppress("SameParameterValue")
        defaultResult: T,
        block: (support: TransferSupport, transferData: OreTabTransferData, targetPane: OreTabPane, dropPoint: Point, targetTabAndIndex: OreTabAndIndex?) -> T
    ): T {
        support
            ?: return defaultResult

        val transferData = run transferData@ {
            val transferable = support.transferable
                ?: return@transferData null

            if (!transferable.isDataFlavorSupported(OreTabTransferDataFlavor)) {
                return@transferData null
            }

            transferable.getTransferData(OreTabTransferDataFlavor) as? OreTabTransferData
        }
            ?: return defaultResult

        val targetPane = (support.component as? OreTabPane)
            ?: return defaultResult

        val dropPoint = support.dropLocation?.dropPoint
            ?: return defaultResult

        val targetTabAndIndex = targetPane.indexAtLocation(dropPoint.x, dropPoint.y).let { index ->
            if (index < 0) {
                null
            } else {
                val tab = targetPane.model.tabs[index]
                OreTabAndIndex(tab, index)
            }
        }

        return block(support, transferData, targetPane, dropPoint, targetTabAndIndex)
    }

    override fun canImport(transferSupport: TransferSupport?): Boolean {
        return this.defaultImportRun(transferSupport, false) { support, transferData, targetPane, dropPoint, targetTabAndIndex ->
            val result = this.canImport(support, transferData, targetPane, dropPoint, targetTabAndIndex)

            dropPaintData = if (result) {
                OreTabDropPaintData(
                    tab = transferData.tab,
                    sourcePane = transferData.pane,
                    sourceIndex = transferData.index,
                    targetPane = targetPane,
                    targetIndex = targetTabAndIndex?.index
                )
            } else {
                null
            }

            result
        }
    }

    /**
     * Checking the ability to import the [transferData].[tab][OreTabTransferData.tab] into the [targetPane]
     *
     * @param support Object, which encapsulates all relevant details of drag and drop transfer
     * @param transferData Information about dragged tab
     * @param targetPane the tab pane that the mouse cursor was within while dragging the tab
     * @param dropPoint mouse cursor coordinates within `targetPane`
     * @param targetTabAndIndex the tab in the `targetPane` under the mouse cursor and its index (if there is a tab under the cursor)
     *
     * @return `true` if the tab can be imported; otherwise - `false`
     */
    protected open fun canImport(support: TransferSupport, transferData: OreTabTransferData, targetPane: OreTabPane, dropPoint: Point, targetTabAndIndex: OreTabAndIndex?): Boolean {
        if (support.dropAction != MOVE) {
            return false
        }

        val (sourcePane, _, draggedTabIndex) = transferData
        return if (sourcePane == targetPane) {
            val targetIndex = (targetTabAndIndex?.index ?: targetPane.model.tabs.lastIndex)
            (draggedTabIndex != targetIndex)
        } else {
            true
        }
    }

    override fun importData(transferSupport: TransferSupport?): Boolean {
        return this.defaultImportRun(transferSupport, false) { support, transferData, targetPane, dropPoint, targetTabAndIndex ->
            this.importData(support, transferData, targetPane, dropPoint, targetTabAndIndex)
        }
    }

    /**
     * Import [transferData].[tab][OreTabTransferData.tab] into [targetPane]
     *
     * If [canImport] returns `false`, the import is canceled
     *
     * If [transferData].[pane][OreTabTransferData.pane] and [targetPane] are the same panel,
     * then the [moveTab][me.ore.swing.ext.tabs.OreTabModel.moveTab] method is called on its model;
     * otherwise, the dragged tab is removed from the old panel and added to the new one
     *
     * @param support Object, which encapsulates all relevant details of drag and drop transfer
     * @param transferData Information about dragged tab
     * @param targetPane the tab pane that the mouse cursor was within while dragging the tab
     * @param dropPoint mouse cursor coordinates within `targetPane`
     * @param targetTabAndIndex the tab in the `targetPane` under the mouse cursor and its index (if there is a tab under the cursor)
     *
     * @return `true` if the tab was imported; otherwise - `false`
     */
    protected open fun importData(support: TransferSupport, transferData: OreTabTransferData, targetPane: OreTabPane, dropPoint: Point, targetTabAndIndex: OreTabAndIndex?): Boolean {
        if (!this.canImport(support)) {
            return false
        }

        val (sourcePane, draggedTab, draggedTabIndex) = transferData

        if (sourcePane == targetPane) {
            val targetIndex = targetTabAndIndex?.index ?: sourcePane.model.tabs.lastIndex
            sourcePane.model.moveTab(movedIndex = draggedTabIndex, targetIndex = targetIndex)
        } else {
            sourcePane.model.removeTab(draggedTabIndex)
            (targetTabAndIndex?.index).let { targetIndex ->
                val addedTabAndIndex = if (targetIndex == null) {
                    targetPane.model.addTab(draggedTab)
                } else {
                    targetPane.model.addTab(draggedTab, targetIndex)
                }
                targetPane.model.select(addedTabAndIndex.index)
            }
        }

        return true
    }
    // endregion
}
