package me.ore.swing.ext.tabs.dnd

import me.ore.swing.ext.tabs.OreTab
import me.ore.swing.ext.tabs.OreTabPane


/**
 * Drawing information for drop location
 *
 * @param tab Dragged tab
 * @param sourcePane Tab pane, which contains [tab]
 * @param sourceIndex Index of [tab] in [sourcePane]
 * @param targetPane Tab pane in which to draw a drop location
 * @param targetIndex Index of tab in [targetPane] on which to draw the drop location
 */
data class OreTabDropPaintData(
    val tab: OreTab,
    val sourcePane: OreTabPane,
    val sourceIndex: Int,
    val targetPane: OreTabPane,
    val targetIndex: Int?
)
