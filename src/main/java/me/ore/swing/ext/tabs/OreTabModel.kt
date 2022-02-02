package me.ore.swing.ext.tabs


/**
 * Tab model for [OreTabPane]
 *
 * Objects that implement this interface are expected to be used in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
 */
interface OreTabModel {
    // region Listeners
    /**
     * Model listeners
     */
    val listeners: Set<OreTabModelListener>

    /**
     * Adding a listener
     *
     * @param listener Added listener
     *
     * @return `true` if the listener was not already in the model and was added by the current method call; otherwise - `false`
     */
    fun addListener(listener: OreTabModelListener): Boolean

    /**
     * Removing a listener
     *
     * @param listener Removed listener
     *
     * @return `true` if the listener was in the model and was removed by the current call; otherwise - `false`
     */
    fun removeListener(listener: OreTabModelListener): Boolean
    // endregion


    // region Tabs
    /**
     * Tabs
     */
    val tabs: List<OreTab>

    /**
     * Adding a tab
     *
     * @param tab Tab to be added
     * @param index The index that the tab should receive; to add a tab at the end, the index must be equal to [tabs].[size][List.size]
     *
     * @return The added tab and the index it got
     *
     * @throws IllegalArgumentException If the tab is already in the model
     * @throws IndexOutOfBoundsException If [index] is less than `0` or greater than [tabs].[size][List.size]
     */
    fun addTab(tab: OreTab, index: Int): OreTabAndIndex

    /**
     * Adding a tab to the end
     *
     * @param tab Tab to be added
     *
     * @return The added tab and the index it got
     *
     * @throws IllegalArgumentException If the tab is already in the model
     */
    fun addTab(tab: OreTab): OreTabAndIndex

    /**
     * Removing a tab
     *
     * @param index Index of tab to remove
     *
     * @return Removed tab and the index it had
     *
     * @throws IndexOutOfBoundsException If [index] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex]
     */
    fun removeTab(index: Int): OreTabAndIndex

    /**
     * Removing a tab
     *
     * @param tab Tab to be removed
     *
     * @return Removed tab and the index it had
     *
     * @throws IllegalArgumentException If the tab was not in the model
     */
    fun removeTab(tab: OreTab): OreTabAndIndex

    /**
     * Moving a tab
     *
     * If [movedIndex] is equal to [targetIndex], then there will be no actual movement.
     * The method will return a result object where [movedTab][OreTabMoveResult.movedTab] will be equal to [targetTab][OreTabMoveResult.targetTab],
     * and [movedOldIndex][OreTabMoveResult.movedOldIndex], [movedNewIndex][OreTabMoveResult.movedNewIndex],
     * [targetOldIndex][OreTabMoveResult.targetOldIndex] and [targetNewIndex][OreTabMoveResult.targetNewIndex] will be equal to each other.
     * Also in this case, the [tabMoved][OreTabModelListener.tabMoved] method is not called on [listeners]
     *
     * _Note: the order of the tab with [movedIndex] and the tab with [targetIndex] is changed, i.e. if the tab with [movedIndex] was before the tab with [targetIndex],
     * then after the move the tab with [movedIndex] will be after the tab with [targetIndex]; and vice versa_
     *
     * @param movedIndex Index of tab to be moved
     * @param targetIndex The index that the tab should get after moving
     *
     * @return Move result
     *
     * @throws IndexOutOfBoundsException If [movedIndex] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex];
     * if [targetIndex] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex]
     */
    fun moveTab(movedIndex: Int, targetIndex: Int): OreTabMoveResult

    /**
     * Moving a tab
     *
     * If the index of [movedTab] is equal to [targetIndex], then there will be no actual movement.
     * The method will return a result object where [movedTab][OreTabMoveResult.movedTab] will be equal to [targetTab][OreTabMoveResult.targetTab],
     * and [movedOldIndex][OreTabMoveResult.movedOldIndex], [movedNewIndex][OreTabMoveResult.movedNewIndex],
     * [targetOldIndex][OreTabMoveResult.targetOldIndex] and [targetNewIndex][OreTabMoveResult.targetNewIndex] will be equal to each other.
     * Also in this case, the [tabMoved][OreTabModelListener.tabMoved] method is not called on [listeners]
     *
     * _Note: the order of [movedTab] and tabs with [targetIndex] is changed, i.e. if [movedTab] was before the tab with [targetIndex],
     * then after the move [movedTab] will be after the tab with [targetIndex]; and vice versa_
     *
     * @param movedTab Tab to be moved
     * @param targetIndex The index that the tab should get after moving
     *
     * @return Move result
     *
     * @throws IllegalArgumentException If [movedTab] is not in the model
     * @throws IndexOutOfBoundsException If [targetIndex] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex]
     */
    @Suppress("unused")
    fun moveTab(movedTab: OreTab, targetIndex: Int): OreTabMoveResult {
        val movedIndex = this.tabs.indexOf(movedTab)
        if (movedIndex < 0)
            throw IllegalArgumentException("Cannot move tab - moved tab not found in model")
        return this.moveTab(movedIndex, targetIndex)
    }

    /**
     * Moving a tab
     *
     * If [movedIndex] is equal to the index of [targetTab], then there will be no actual movement.
     * The method will return a result object where [movedTab][OreTabMoveResult.movedTab] will be equal to [targetTab][OreTabMoveResult.targetTab],
     * and [movedOldIndex][OreTabMoveResult.movedOldIndex], [movedNewIndex][OreTabMoveResult.movedNewIndex],
     * [targetOldIndex][OreTabMoveResult.targetOldIndex] and [targetNewIndex][OreTabMoveResult.targetNewIndex] will be equal to each other.
     * Also in this case, the [tabMoved][OreTabModelListener.tabMoved] method is not called on [listeners]
     *
     * _Note: the order of tab with [movedIndex] and [targetTab] is changed, i.e. if the tab with [movedIndex] was before [targetTab],
     * then after the move the tab with [movedIndex] will be after [targetTab]; and vice versa_
     *
     * @param movedIndex Index of tab to be moved
     * @param targetTab The tab where the tab with [movedIndex] should be
     *
     * @return Move result
     *
     * @throws IllegalArgumentException If [targetTab] is not found in the model
     * @throws IndexOutOfBoundsException If [movedIndex] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex]
     */
    @Suppress("unused")
    fun moveTab(movedIndex: Int, targetTab: OreTab): OreTabMoveResult {
        val targetIndex = this.tabs.indexOf(targetTab)
        if (targetIndex < 0)
            throw IllegalArgumentException("Cannot move tab - target tab not found in model")
        return this.moveTab(movedIndex, targetIndex)
    }

    /**
     * Moving a tab
     *
     * If [movedTab] and [targetTab] are the same tab, then there will be no actual movement.
     * The method will return a result object where [movedTab][OreTabMoveResult.movedTab] will be equal to [targetTab][OreTabMoveResult.targetTab],
     * and [movedOldIndex][OreTabMoveResult.movedOldIndex], [movedNewIndex][OreTabMoveResult.movedNewIndex],
     * [targetOldIndex][OreTabMoveResult.targetOldIndex] and [targetNewIndex][OreTabMoveResult.targetNewIndex] will be equal to each other.
     * Also in this case, the [tabMoved][OreTabModelListener.tabMoved] method is not called on [listeners]
     *
     * _Note: the order of [movedTab] and [targetTab] is reversed, i.e. if [movedTab] was before [targetTab],
     * then after moving [movedTab] will be after [targetTab]; and vice versa_
     *
     * @param movedTab Tab to be moved
     * @param targetTab The tab where [movedTab] should be
     *
     * @return Move result
     *
     * @throws IllegalArgumentException If [movedTab] is not found in the model; if [targetTab] is not found in the model
     */
    @Suppress("unused")
    fun moveTab(movedTab: OreTab, targetTab: OreTab): OreTabMoveResult {
        if (movedTab == targetTab) {
            val index = this.tabs.indexOf(movedTab)
            if (index < 0)
                throw IllegalArgumentException("Cannot move tab - moved tab not found in model")

            return OreTabMoveResult(
                movedTab = movedTab,
                movedOldIndex = index,
                movedNewIndex = index,
                targetTab = movedTab,
                targetOldIndex = index,
                targetNewIndex = index
            )
        }

        val movedIndex = this.tabs.indexOf(movedTab)
        if (movedIndex < 0)
            throw IllegalArgumentException("Cannot move tab - moved tab not found in model")

        val targetIndex = this.tabs.indexOf(targetTab)
        if (targetIndex < 0)
            throw IllegalArgumentException("Cannot move tab - target tab not found in model")

        return this.moveTab(movedIndex, targetIndex)
    }
    // endregion


    // region Selection
    /**
     * Selected tab and its index
     */
    val selection: OreTabAndIndex?

    /**
     * Selected tab
     */
    @Suppress("unused")
    val selectedTab: OreTab?
        get() = this.selection?.tab

    /**
     * Index of selected tab
     */
    @Suppress("unused")
    val selectedIndex: Int?
        get() = this.selection?.index

    /**
     * Change tab selection
     *
     * @param index Index of the tab to be selected
     *
     * @return Selected tab and its index
     *
     * @throws IndexOutOfBoundsException If [index] is less than `0` or greater than [tabs].[lastIndex][List.lastIndex]
     */
    fun select(index: Int): OreTabAndIndex

    /**
     * Change tab selection
     *
     * @param tab Tab to be selected
     *
     * @return Selected tab and its index
     *
     * @throws IllegalArgumentException If [tab] is not in the model
     */
    fun select(tab: OreTab): OreTabAndIndex
    // endregion
}
