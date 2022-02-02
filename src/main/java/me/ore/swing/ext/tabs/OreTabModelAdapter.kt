package me.ore.swing.ext.tabs


/**
 * "Empty" implementation of [OreTabModelListener]
 *
 * It is used if not all [OreTabModelListener] methods need to be implemented.
 *
 * There is no code in all methods, each of them can be overridden
 */
open class OreTabModelAdapter: OreTabModelListener {
    override fun tabAdded(model: OreTabModel, tabAndIndex: OreTabAndIndex) {}

    override fun tabRemoved(model: OreTabModel, tabAndIndex: OreTabAndIndex) {}

    override fun tabMoved(model: OreTabModel, moveResult: OreTabMoveResult) {}

    override fun tabSelectionAppear(model: OreTabModel, newSelection: OreTabAndIndex) {}

    override fun tabSelectionChanged(model: OreTabModel, oldSelection: OreTabAndIndex, newSelection: OreTabAndIndex) {}

    override fun tabSelectionLost(model: OreTabModel, oldSelection: OreTabAndIndex) {}
}
