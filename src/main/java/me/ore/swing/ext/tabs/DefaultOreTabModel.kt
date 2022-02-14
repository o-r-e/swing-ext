package me.ore.swing.ext.tabs

import me.ore.swing.ext.OreSwingExt
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


/**
 * The default implementation of the tab model
 */
open class DefaultOreTabModel: OreTabModel {
    // region Listeners
    /**
     * A set of listeners; mutable version of [listeners]
     */
    @Suppress("PropertyName")
    protected open val _listeners: MutableSet<OreTabModelListener> = HashSet()

    override val listeners: Set<OreTabModelListener> by lazy(LazyThreadSafetyMode.NONE) { Collections.unmodifiableSet(this._listeners) }

    override fun addListener(listener: OreTabModelListener): Boolean = this._listeners.add(listener)

    override fun removeListener(listener: OreTabModelListener): Boolean = this._listeners.remove(listener)

    /**
     * Executes a [block] for each listener in [listeners]
     *
     * @param block Executable block
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun forEachListener(block: (listener: OreTabModelListener) -> Unit) {
        this.listeners.toList().forEach { listener ->
            try {
                block(listener)
            } catch (e: Exception) {
                OreSwingExt.handle(e)
            }
        }
    }
    // endregion


    // region Tabs
    /**
     * List of tabs; mutable version of [tabs]
     */
    @Suppress("PropertyName")
    protected open val _tabs: MutableList<OreTab> = ArrayList()

    override val tabs: List<OreTab> by lazy(LazyThreadSafetyMode.NONE) { Collections.unmodifiableList(this._tabs) }

    override fun addTab(tab: OreTab, index: Int): OreTabAndIndex {
        if (this._tabs.contains(tab)) {
            throw IllegalArgumentException("Tab already present in model")
        }

        if (index < 0) {
            throw IndexOutOfBoundsException("Index $index is less than 0")
        }
        this._tabs.size.let { size ->
            if (index > size) {
                throw IndexOutOfBoundsException("Index $index is greater than size of tabs $size")
            }
        }

        this._tabs.add(index, tab)
        val result = OreTabAndIndex(tab, index)

        val selected: Boolean
        if (this._tabs.size == 1) {
            this._selection = result
            selected = true
        } else {
            selected = false
        }

        this.forEachListener { it.tabAdded(this, result) }
        if (selected) {
            this.forEachListener { it.tabSelectionAppear(this, result) }
        }

        return result
    }

    override fun addTab(tab: OreTab): OreTabAndIndex = this.addTab(tab, this.tabs.size)

    override fun removeTab(index: Int): OreTabAndIndex {
        if (index < 0) {
            throw IndexOutOfBoundsException("Index $index is less than 0")
        }
        this._tabs.lastIndex.let {
            if (index > it) {
                throw IndexOutOfBoundsException("Index $index is greater than maximal index $it")
            }
        }

        val tab = this._tabs.removeAt(index)
        val result = OreTabAndIndex(tab, index)

        val oldSelection = this._selection
        val newSelection: OreTabAndIndex? = when {
            (this._tabs.isEmpty()) -> null
            (oldSelection == null) -> OreTabAndIndex(this._tabs[0], 0)
            (oldSelection.tab == tab) -> {
                val lastIndex = this._tabs.lastIndex
                if (index <= lastIndex) {
                    OreTabAndIndex(this._tabs[index], index)
                } else {
                    OreTabAndIndex(this._tabs[lastIndex], lastIndex)
                }
            }
            else -> {
                val selectedTab = oldSelection.tab
                OreTabAndIndex(selectedTab, this._tabs.indexOf(selectedTab))
            }
        }

        if (oldSelection != newSelection) {
            this._selection = newSelection
        }

        this.forEachListener { it.tabRemoved(this, result) }
        if (oldSelection != newSelection) {
            when {
                (oldSelection == null) -> {
                    if (newSelection != null) {
                        this.forEachListener { it.tabSelectionAppear(this, newSelection) }
                    }
                }
                (newSelection == null) -> this.forEachListener { it.tabSelectionLost(this, oldSelection) }
                else -> this.forEachListener { it.tabSelectionChanged(this, oldSelection, newSelection) }
            }
        }

        return result
    }

    override fun removeTab(tab: OreTab): OreTabAndIndex {
        val index = this._tabs.indexOf(tab)
        if (index < 0) {
            throw IllegalArgumentException("Cannot find tab in model")
        }
        return this.removeTab(index)
    }

    override fun moveTab(movedIndex: Int, targetIndex: Int): OreTabMoveResult {
        if (movedIndex < 0)
            throw IndexOutOfBoundsException("Moved index $movedIndex is less than 0")
        if (targetIndex < 0)
            throw IndexOutOfBoundsException("Target index $targetIndex is less than 0")

        this._tabs.lastIndex.let {
            if (movedIndex > it)
                throw IndexOutOfBoundsException("Moved index $movedIndex is greater than maximal index $it")
            if (targetIndex > it)
                throw IndexOutOfBoundsException("Target index $targetIndex is greater than maximal index $it")
        }

        if (movedIndex == targetIndex) {
            val tab = this._tabs[movedIndex]
            return OreTabMoveResult(
                movedTab = tab,
                movedOldIndex = movedIndex,
                movedNewIndex = movedIndex,
                targetTab = tab,
                targetOldIndex = movedIndex,
                targetNewIndex = movedIndex
            )
        }

        val targetTab = this._tabs[targetIndex]
        val movedTab = this._tabs.removeAt(movedIndex)
        this._tabs.add(targetIndex, movedTab)

        val result = OreTabMoveResult(
            movedTab = movedTab,
            movedOldIndex = movedIndex,
            movedNewIndex = targetIndex,
            targetTab = targetTab,
            targetOldIndex = targetIndex,
            targetNewIndex = this._tabs.indexOf(targetTab)
        )

        val oldSelection = this._selection
        val newSelection = run {
            val selectedTab = oldSelection?.tab ?: this._tabs.first()
            val selectedIndex = this._tabs.indexOf(selectedTab)
            OreTabAndIndex(selectedTab, selectedIndex)
        }
        this._selection = newSelection

        this.forEachListener { it.tabMoved(this, result) }
        if (oldSelection != newSelection) {
            if (oldSelection == null) {
                this.forEachListener { it.tabSelectionAppear(this, newSelection) }
            } else {
                this.forEachListener { it.tabSelectionChanged(this, oldSelection, newSelection) }
            }
        }

        return result
    }
    //endregion


    // region Selection
    /**
     * Selected tab and its index; mutable version of [selection]
     */
    @Suppress("PropertyName")
    protected open var _selection: OreTabAndIndex? = null

    override val selection: OreTabAndIndex?
        get() = this._selection

    override fun select(index: Int): OreTabAndIndex {
        if (index < 0) {
            throw IndexOutOfBoundsException("Index $index is less than 0")
        }
        this.tabs.lastIndex.let {
            if (index > it) {
                throw IndexOutOfBoundsException("Index $index is greater than maximal index $it")
            }
        }

        val tab = this.tabs[index]
        val result = OreTabAndIndex(tab, index)

        val oldSelection = this._selection
        this._selection = result
        if (oldSelection != result) {
            when {
                (oldSelection == null) -> this.forEachListener { it.tabSelectionAppear(this, result) }
                else -> this.forEachListener { it.tabSelectionChanged(this, oldSelection, result) }
            }
        }

        return result
    }

    override fun select(tab: OreTab): OreTabAndIndex {
        val index = this.tabs.indexOf(tab)
        if (index < 0)
            throw IllegalArgumentException("Tab not present in model")

        return this.select(index)
    }
    // endregion
}
