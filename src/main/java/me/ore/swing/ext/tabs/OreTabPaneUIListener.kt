package me.ore.swing.ext.tabs

import java.awt.event.MouseEvent


/**
 * [OreTabPaneUI]'s events listener
 */
interface OreTabPaneUIListener {
    /**
     * Called when the mouse cursor moves over a tab while the mouse button is pressed
     *
     * @param ui Additional UI layer
     * @param event Mouse event
     * @param index Index of tab
     */
    fun tabDragged(ui: OreTabPaneUI, event: MouseEvent, index: Int)

    /**
     * Called when the close tab button is clicked
     *
     * @param ui Additional UI layer
     * @param index Index of tab
     */
    fun tabCloseClicked(ui: OreTabPaneUI, index: Int)

    /**
     * Called when the settings of the additional UI layer are changed
     *
     * Usually means that the component needs to be rerendered
     *
     * @param ui Additional UI layer
     */
    fun uiConfigChanged(ui: OreTabPaneUI)
}
