package me.ore.swing.ext.tabs


/**
 * Tab move result
 *
 * @param movedTab Moved Tab
 * @param movedOldIndex Old index of [movedTab]
 * @param movedNewIndex New index of [movedTab]
 * @param targetTab The tab to which the [movedTab] was moved
 * @param targetOldIndex Old index of [targetTab]
 * @param targetNewIndex New index of [targetTab]
 */
data class OreTabMoveResult(
    val movedTab: OreTab,
    val movedOldIndex: Int,
    val movedNewIndex: Int,
    val targetTab: OreTab,
    val targetOldIndex: Int,
    val targetNewIndex: Int
)
