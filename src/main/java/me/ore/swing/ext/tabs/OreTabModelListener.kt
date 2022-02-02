package me.ore.swing.ext.tabs


/**
 * Event listener for [OreTabModel]
 */
interface OreTabModelListener {
    /**
     * Called when a tab is added to the model
     *
     * @param model Model
     * @param tabAndIndex Tab and its index
     */
    fun tabAdded(model: OreTabModel, tabAndIndex: OreTabAndIndex)

    /**
     * Called when a tab is removed from the model
     *
     * @param model Model
     * @param tabAndIndex Tab and its index
     */
    fun tabRemoved(model: OreTabModel, tabAndIndex: OreTabAndIndex)

    /**
     * Called when a tab is moved
     *
     * @param model Model
     * @param moveResult Move result
     */
    fun tabMoved(model: OreTabModel, moveResult: OreTabMoveResult)

    /**
     * Called when the selected tab appears in the model
     *
     * Should only be called when the first tab is added to the model
     *
     * @param model Model
     * @param newSelection Selected tab and its index
     */
    fun tabSelectionAppear(model: OreTabModel, newSelection: OreTabAndIndex)

    /**
     * Called when the selected tab and/or index of the selected tab is changed
     *
     * Only the index of the selected tab can change (for example, when deleting a tab before the selected one);
     * conversely, only the selected tab can change (e.g. when deleting the selected tab)
     *
     * @param model Model
     * @param oldSelection Previously selected tab and its index
     * @param newSelection New selected tab and its index
     */
    fun tabSelectionChanged(model: OreTabModel, oldSelection: OreTabAndIndex, newSelection: OreTabAndIndex)

    /**
     * Called when the selected tab disappears (when the last model tab is removed)
     *
     * @param model Model
     * @param oldSelection Previously selected tab and its index
     */
    fun tabSelectionLost(model: OreTabModel, oldSelection: OreTabAndIndex)
}
