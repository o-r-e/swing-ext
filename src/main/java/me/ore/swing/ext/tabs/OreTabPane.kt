package me.ore.swing.ext.tabs

import me.ore.swing.ext.OreDnDImageRender
import me.ore.swing.ext.OreSwingExt
import me.ore.swing.ext.tabs.dnd.OreTabDropPaintData
import java.awt.BorderLayout
import java.awt.Component
import java.awt.EventQueue
import java.awt.Rectangle
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.lang.ref.WeakReference
import java.util.*
import javax.swing.*
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener


/**
 * Tab bar for [OreTab]s
 *
 * Uses [JTabbedPane] and [JLayer]
 *
 * Objects of this class must be used in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
 *
 * @param tabPlacement tab layout; default - [TabPlacement.TOP]
 * @param tabLayoutPolicy Tab layout policy when "overflowing"; default - [TabLayoutPolicy.WRAP]
 * @param model The tab model to be used in the panel
 */
@Suppress("LeakingThis")
open class OreTabPane(
    tabPlacement: TabPlacement = TabPlacement.TOP,
    tabLayoutPolicy: TabLayoutPolicy = TabLayoutPolicy.WRAP,
    model: OreTabModel = DefaultOreTabModel()
): JPanel(BorderLayout()) {
    companion object {
        /**
         * Name of property [model]
         *
         * Used when calling the [PropertyChangeListener]s when the [model] changes
         */
        const val PROPERTY__MODEL = "model"

        /**
         * Name of property [tabPlacement]
         *
         * Used when calling [PropertyChangeListener]s when [tabPlacement] changes
         */
        const val PROPERTY__TAB_PLACEMENT = "tabPlacement"

        /**
         * Name of property [tabLayoutPolicy]
         *
         * Used when calling [PropertyChangeListener]s when [tabLayoutPolicy] changes
         */
        const val PROPERTY__TAB_LAYOUT_POLICY = "tabLayoutPolicy"


        // region Binding tabs and panes
        /**
         * Map "tab -> reference to panel"
         */
        private val TAB_TO_PANE_MAP = WeakHashMap<OreTab, WeakReference<OreTabPane>>()

        /**
         * Change the panel to which a tab belongs
         *
         * If the tab has already been linked to another panel, then the tab is removed from the previous panel
         *
         * @param tab The tab for which the panel is changing
         * @param pane New panel for tab
         */
        private fun setPaneForTab(tab: OreTab, pane: OreTabPane?) {
            val old = synchronized(TAB_TO_PANE_MAP) {
                val oldReference = TAB_TO_PANE_MAP[tab]
                val old = oldReference?.get()
                if (old == pane) {
                    null
                } else {
                    oldReference?.clear()
                    if (pane == null) {
                        TAB_TO_PANE_MAP.remove(tab)
                    } else {
                        TAB_TO_PANE_MAP[tab] = WeakReference(pane)
                    }
                    old
                }
            }

            if (old != pane) {
                try {
                    old?.model?.let {
                        if (it.tabs.contains(tab)) {
                            it.removeTab(tab)
                        }
                    }
                } catch (e: Exception) {
                    OreSwingExt.handle(e)
                }

                try {
                    tab.updatePane()
                } catch (e: Exception) {
                    OreSwingExt.handle(e)
                }
            }
        }

        /**
         * Getting the panel for a tab
         *
         * @param tab The tab to get the panel for
         *
         * @return The panel that the tab is linked to
         */
        fun getPaneForTab(tab: OreTab): OreTabPane? {
            return synchronized(TAB_TO_PANE_MAP) {
                val reference = TAB_TO_PANE_MAP[tab]
                if (reference == null) {
                    null
                } else {
                    val result = reference.get()
                    if (result == null) {
                        TAB_TO_PANE_MAP.remove(tab)
                    }
                    result
                }
            }
        }
        // endregion
    }


    // region Components
    /**
     * Create [pane]
     *
     * __Attention! Called in the constructor of the [OreTabPane] class__
     *
     * @param tabPlacement Tab placement
     * @param tabLayoutPolicy Tab layout policy when "overflowing"
     *
     * @return Internal tab pane
     */
    protected open fun createPane(tabPlacement: TabPlacement, tabLayoutPolicy: TabLayoutPolicy): JTabbedPane = JTabbedPane(tabPlacement.swingValue, tabLayoutPolicy.swingValue)

    /**
     * Internal tab pane
     *
     * @see layer
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val pane: JTabbedPane = this.createPane(tabPlacement, tabLayoutPolicy)


    /**
     * Create [paneUI]
     *
     * __Attention! Called in the constructor of the [OreTabPane] class__
     *
     * @return Additional UI layer
     */
    protected open fun createPaneUI(): OreTabPaneUI = OreTabPaneUI()

    /**
     * Additional UI layer
     *
     * @see layer
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val paneUI: OreTabPaneUI = this.createPaneUI()


    /**
     * Create [layer]
     *
     * __Attention! Called in the constructor of the [OreTabPane] class__
     *
     * @param pane Internal tab pane
     * @param paneUI Additional UI layer
     *
     * @return Decorator for internal tab pane
     */
    protected open fun createLayer(pane: JTabbedPane, paneUI: OreTabPaneUI): JLayer<JTabbedPane> = JLayer(pane, paneUI)

    /**
     * Decorator for internal tab pane
     *
     * Applies [paneUI] "on top" of [pane]
     *
     * Added to the current panel when it is created
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected val layer: JLayer<JTabbedPane> = this.createLayer(this.pane, this.paneUI)
    // endregion


    // region Model, model listener
    /**
     * [OreTabModelListener] implementation that redirects model events to [pane] methods
     *
     * @param pane Panel where model events are redirected
     */
    private class ModelListener(pane: OreTabPane): OreTabModelListener {
        /**
         * Weak reference to [OreTabPane]
         */
        private val paneReference = WeakReference(pane)

        /**
         * Model event default handling
         *
         * If [paneReference] does not contain a pane, the current listener is removed from [model]; otherwise, [block] is called, into which the found panel is passed
         *
         * @param model Tab model
         * @param block Executable block
         */
        private inline fun defaultRun(model: OreTabModel, block: (pane: OreTabPane) -> Unit) {
            val pane = this.paneReference.get()
            if (pane == null) {
                model.removeListener(this)
                return
            }

            block(pane)
        }

        override fun tabAdded(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
            this.defaultRun(model) {
                it.tabAdded(tabAndIndex.tab, tabAndIndex.index)
            }
        }

        override fun tabRemoved(model: OreTabModel, tabAndIndex: OreTabAndIndex) {
            this.defaultRun(model) {
                it.tabRemoved(tabAndIndex.tab, tabAndIndex.index)
            }
        }

        override fun tabMoved(model: OreTabModel, moveResult: OreTabMoveResult) {
            this.defaultRun(model) {
                it.tabMoved(moveResult)
            }
        }

        override fun tabSelectionAppear(model: OreTabModel, newSelection: OreTabAndIndex) {
            this.defaultRun(model) {
                it.tabSelectionAppear(newSelection)
            }
        }

        override fun tabSelectionChanged(model: OreTabModel, oldSelection: OreTabAndIndex, newSelection: OreTabAndIndex) {
            this.defaultRun(model) {
                it.tabSelectionChanged(oldSelection, newSelection)
            }
        }

        override fun tabSelectionLost(model: OreTabModel, oldSelection: OreTabAndIndex) {
            this.defaultRun(model) {
                it.tabSelectionLost(oldSelection)
            }
        }
    }


    /**
     * Listener for [model]
     */
    private val modelListener = ModelListener(this)

    /**
     * Tab model
     */
    open var model: OreTabModel = model
        set(new) {
            if (field != new) {
                val old = field
                field = new
                this.forgetOldModel(old)
                this.applyNewModel(new)
                this.firePropertyChange(PROPERTY__MODEL, old, new)
            }
        }

    /**
     * Deleting all data of the old model, deleting the listener of the old model
     *
     * Called on the old model when [model][OreTabPane.model] changes
     *
     * @param model Old model
     */
    protected open fun forgetOldModel(model: OreTabModel) {
        this.pane.removeAll()
        model.removeListener(this.modelListener)
    }

    /**
     * Applying a new model - adding a listener to the model, adding model tabs to the current pane
     *
     * Called when the current panel is created and for a new model when [model][OreTabPane.model] changes
     *
     * @param model New model
     */
    protected open fun applyNewModel(model: OreTabModel) {
        model.addListener(this.modelListener)
        model.tabs.forEachIndexed { index, tab -> this.tabAdded(tab, index) }
    }
    // endregion


    // region Tabs
    /**
     * Tab property listener implementation that redirects all events to [pane]
     *
     * If no pane is found when calling [TabListener.propertyChange] ([paneReference].[get][WeakReference.get] returns `null`),
     * the listener registration is removed from the tab that the property change event belongs to
     */
    private class TabListener(pane: OreTabPane): PropertyChangeListener {
        companion object {
            /**
             * An array of property names handled by listeners of this class
             */
            private val TAB_PROPERTY_NAMES = arrayOf(OreTab.PROPERTY__CONTENT, OreTab.PROPERTY__TITLE, OreTab.PROPERTY__CLOSEABLE)
        }


        /**
         * Weak reference to panel
         */
        private val paneReference = WeakReference(pane)


        // region Register, deregister
        /**
         * Registering a listener for [tab] properties
         *
         * @param tab The tab where the registration of the current listener will be added
         */
        fun register(tab: OreTab) {
            TAB_PROPERTY_NAMES.forEach {
                tab.addPropertyChangeListener(it, this)
            }
        }

        /**
         * Remove listener registration for [tab] properties
         *
         * @param tab The tab that will have the registration of the current listener removed
         */
        fun deregister(tab: OreTab) {
            TAB_PROPERTY_NAMES.forEach {
                tab.removePropertyChangeListener(it, this)
            }
        }
        // endregion


        override fun propertyChange(event: PropertyChangeEvent?) {
            event ?: return

            val propertyName = event.propertyName?.trim()?.takeIf { it.isNotEmpty() }
                ?: return

            val tab = event.source as? OreTab
                ?: return

            val pane = this.paneReference.get()
            if (pane == null) {
                this.deregister(tab)
                return
            }

            val index = pane.model.tabs.indexOf(tab)
            val old: Any? = event.oldValue
            val new: Any? = event.newValue

            when (propertyName) {
                OreTab.PROPERTY__CONTENT -> {
                    if (((old == null) || (old is Component)) && ((new == null) || (new is Component))) {
                        pane.tabContentChanged(tab, index, old as? Component, new as? Component)
                    }
                }
                OreTab.PROPERTY__TITLE -> {
                    if (((old == null) || (old is Component)) && ((new == null) || (new is Component))) {
                        pane.tabTitleChanged(tab, index, old as? Component, new as? Component)
                    }
                }
                OreTab.PROPERTY__CLOSEABLE -> {
                    if ((old is Boolean) && (new is Boolean)) {
                        pane.tabCloseableChanged(tab, index, old, new)
                    }
                }
            }
        }
    }

    /**
     * Property change event listener for tabs
     */
    private val tabListener = TabListener(this)


    /**
     * Called when a tab is added to [model]
     *
     * @param tab Added tab
     * @param index Index of [tab]
     */
    protected open fun tabAdded(tab: OreTab, index: Int) {
        this.tabListener.register(tab)
        this.pane.insertTab(null, null, tab.content, null, index)
        this.pane.setTabComponentAt(index, OreTabTitle.create(this.paneUI, tab))
        setPaneForTab(tab, this)
    }

    /**
     * Called when a tab is removed from [model]
     *
     * @param tab Removed tab
     * @param index Index of [tab]
     */
    protected open fun tabRemoved(tab: OreTab, index: Int) {
        this.pane.removeTabAt(index)
        this.tabListener.deregister(tab)
    }

    /**
     * Called when a tab is moved in [model]
     *
     * @param moveResult Move result
     */
    protected open fun tabMoved(moveResult: OreTabMoveResult) {
        val oldIndex = moveResult.movedOldIndex
        val newIndex = moveResult.movedNewIndex
        val tab = moveResult.movedTab

        this.pane.removeTabAt(oldIndex)
        this.pane.insertTab(null, null, tab.content, null, newIndex)
        this.pane.setTabComponentAt(newIndex, OreTabTitle.create(this.paneUI, tab))
    }

    /**
     * Called when the selected tab appears in the [model] (usually when the first tab is added)
     *
     * @param newSelection Selected tab and its index
     */
    protected open fun tabSelectionAppear(newSelection: OreTabAndIndex) {
        val index = newSelection.index
        if (this.pane.selectedIndex != index) {
            this.pane.selectedIndex = index
        }
    }

    /**
     * Called when the selected tab and/or its index in [model] changes
     *
     * @param oldSelection Old selected tab and its index
     * @param newSelection New selected tab and its index
     */
    protected open fun tabSelectionChanged(oldSelection: OreTabAndIndex, newSelection: OreTabAndIndex) {
        val index = newSelection.index
        if (this.pane.selectedIndex != index) {
            this.pane.selectedIndex = index
        }
    }

    /**
     * Called when the selected tab in the [model] disappears (usually when the last tab is removed)
     *
     * @param oldSelection Old selected tab and its index
     */
    protected open fun tabSelectionLost(oldSelection: OreTabAndIndex) {}


    /**
     * Called when the mouse cursor is moved on the tab while the left mouse button is pressed (i.e. when the Drag-and-Drop mechanism can be triggered)
     *
     * When called, the [_draggedTabAndIndex] property is filled
     *
     * Fires a [MouseEvent] event with [MouseEvent.id] equal to [MouseEvent.MOUSE_DRAGGED] for the current panel
     *
     * If [useTransferHandlerOnTabDragged] is `true` and [getTransferHandler] returns a handler (not null),
     * then it starts the Drag-and-Drop mechanism by calling the [exportAsDrag][TransferHandler.exportAsDrag] method on that handler.
     * If at the same time [useDndImageRendererOnTabDragged] is `true`, then [OreDnDImageRender.image] is set to the image for [tab]
     *
     * @param event Tab drag start event (comes from [paneUI])
     * @param tab Dragged tab
     * @param index Index of [tab]
     */
    protected open fun tabDragged(event: MouseEvent, tab: OreTab, index: Int) {
        this._draggedTabAndIndex = OreTabAndIndex(tab, index)
        val newEvent = MouseEvent(
            this,
            MouseEvent.MOUSE_DRAGGED,
            System.currentTimeMillis(),
            event.modifiersEx,
            event.x,
            event.y,
            event.xOnScreen,
            event.yOnScreen,
            event.clickCount,
            event.isPopupTrigger,
            event.button
        )
        this.dispatchEvent(newEvent)

        if (this.useTransferHandlerOnTabDragged) {
            this.transferHandler?.let {
                it.exportAsDrag(this, newEvent, TransferHandler.MOVE)
                if (this.useDndImageRendererOnTabDragged) {
                    this.pane.getBoundsAt(index)?.let { tabBounds ->
                        val image = OreSwingExt.getImageOf(this.pane, tabBounds)
                        if ((image.width > 0) && (image.height > 0)) {
                            OreDnDImageRender.image = image
                        }
                    }
                }
            }
        }
    }

    /**
     * Called when the user clicks on the close tab button
     *
     * If [tab].[closeClicked][OreTab.closeClicked] returns `true`, removes [tab] from [model]
     *
     * @param tab The tab the user wants to close
     * @param index Index of [tab]
     */
    protected open fun tabCloseClicked(tab: OreTab, index: Int) {
        if (tab.closeClicked()) {
            this.model.removeTab(index)
        }
    }


    /**
     * Gets or creates (if it doesn't already exist) the title component of the tab for the [tab]
     *
     * If a tab title component was created when this method was called, adds it to [pane] and calls [processNew] on it;
     * if the tab title component already existed - calls [processExistent] for it
     *
     * @param tab The tab to get the title for
     * @param index Index of [tab]
     * @param processNew Called for the created title component
     * @param processExistent Called on an existing title component
     *
     * @return Title component for [tab]
     */
    private inline fun getOrCreateTabTitle(
        tab: OreTab,
        index: Int,
        processNew: (title: OreTabTitle) -> Unit = {},
        processExistent: (title: OreTabTitle) -> Unit
    ): OreTabTitle {
        val existentTitle = this.pane.getTabComponentAt(index) as? OreTabTitle
        return if (existentTitle == null) {
            val newTitle = OreTabTitle.create(this.paneUI, tab)
            this.pane.setTabComponentAt(index, newTitle)
            processNew(newTitle)
            newTitle
        } else {
            processExistent(existentTitle)
            existentTitle
        }
    }


    /**
     * Called when the content of a tab changes
     *
     * @param tab Tab
     * @param index Index of [tab]
     * @param old Old content
     * @param new New content
     */
    protected open fun tabContentChanged(tab: OreTab, index: Int, old: Component?, new: Component?) {
        this.pane.setComponentAt(index, new)
    }

    /**
     * Called when the title of a tab changes
     *
     * @param tab Tab
     * @param index Index of [tab]
     * @param old Old title
     * @param new New title
     */
    protected open fun tabTitleChanged(tab: OreTab, index: Int, old: Component?, new: Component?) {
        this.getOrCreateTabTitle(tab, index) {
            it.title = new
        }
    }

    /**
     * Called when the property [closeable][OreTab.closeable] of a tab is changed
     *
     * @param tab Tab
     * @param index Index of [tab]
     * @param old Old value of [closeable][OreTab.closeable]
     * @param new New value of [closeable][OreTab.closeable]
     */
    protected open fun tabCloseableChanged(tab: OreTab, index: Int, old: Boolean, new: Boolean) {
        this.getOrCreateTabTitle(tab, index) {
            it.closeable = new
        }
    }
    // endregion


    // region UI listener
    /**
     * [paneUI] listener implementation
     *
     * Redirects [paneUI] events to [pane]
     *
     * @param pane Panel to which events from [paneUI] will be redirected
     */
    private class UIListener(pane: OreTabPane): OreTabPaneUIListener {
        /**
         * Weak reference to panel
         */
        private val paneReference = WeakReference(pane)

        /**
         * Default handling of event from [ui]
         *
         * If [paneReference].[get][WeakReference.get] returns `null`, the current listener is removed from [ui];
         * otherwise - [block] is called with the resulting panel
         *
         * @param ui Additional UI layer
         * @param block Executable block
         */
        private inline fun defaultRun(ui: OreTabPaneUI, block: (pane: OreTabPane) -> Unit) {
            val pane = this.paneReference.get()
            if (pane == null) {
                ui.removeListener(this)
            } else {
                block(pane)
            }
        }

        /**
         * Default handling of event [ui]
         *
         * If [paneReference].[get][WeakReference.get] returns `null`, the current listener is removed from [ui].
         * Otherwise, [index] is searched for a tab, and if a tab is found, [block] is called with the resulting panel and tab
         *
         * @param ui Additional UI layer
         * @param index Index of tab
         * @param block Executable block
         */
        private inline fun defaultRun(ui: OreTabPaneUI, index: Int, block: (pane: OreTabPane, tab: OreTab) -> Unit) {
            this.defaultRun(ui) {
                val tabs = it.model.tabs
                if (index < tabs.size) {
                    val tab = tabs[index]
                    block(it, tab)
                }
            }
        }


        override fun tabDragged(ui: OreTabPaneUI, event: MouseEvent, index: Int) {
            this.defaultRun(ui, index) { pane, tab -> pane.tabDragged(event, tab, index) }
        }

        override fun tabCloseClicked(ui: OreTabPaneUI, index: Int) {
            this.defaultRun(ui, index) { pane, tab -> pane.tabCloseClicked(tab, index) }
        }

        override fun uiConfigChanged(ui: OreTabPaneUI) {
            this.defaultRun(ui) {
                val closeButtonPlaceSize = ui.closeButtonPlaceSize
                val pane = it.pane
                for (i in 0 until pane.tabCount) {
                    (pane.getTabComponentAt(i) as? OreTabTitle)?.closeButtonSize = closeButtonPlaceSize
                }
            }
        }
    }

    /**
     * [paneUI] events listener
     */
    private val uiListener = UIListener(this)
    // endregion


    /**
     * Returns the tab index corresponding to the tab whose bounds intersect the specified location. Returns `-1` if no tab intersects the location.
     *
     * @param x The x location relative to this pane
     * @param y The y location relative to this pane
     *
     * @return The tab index which intersects the location, or `-1` if no tab intersects the location
     */
    fun indexAtLocation(x: Int, y: Int): Int {
        return this.pane.indexAtLocation(x, y)
    }

    /**
     * Returns the tab bounds at index.
     * If the tab at this index is not currently visible in the UI, then returns `null`.
     *
     * @param index The index to be queried
     *
     * @return A Rectangle containing the tab bounds at index, or `null` if tab at index is not currently visible in the UI
     *
     * @throws IndexOutOfBoundsException If index is out of range `(index < 0 || index >= tab count)`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getBoundsAt(index: Int): Rectangle? {
        return this.pane.getBoundsAt(index)
    }

    /**
     * Returns the tab title image
     *
     * @param index The index of the tab to get the title image for
     *
     * @return Tab title image, or `null` if the tab is not displayed
     *
     * @throws IndexOutOfBoundsException If index is out of range `(index < 0 || index >= tab count)`
     */
    @Suppress("unused")
    fun getTabImage(index: Int): BufferedImage? {
        val bounds = this.getBoundsAt(index)
            ?: return null

        return OreSwingExt.getImageOf(this, bounds)
    }


    // region Drag-and-Drop support
    /**
     * The tab and its index for which the drag start event occurred; mutable version [draggedTabAndIndex]
     *
     * _Note: for various reasons, the property may not become `null` if the Drag-and-Drop process has not started or has already ended_
     *
     * Filled before processing the [MouseEvent] event with [id][MouseEvent.id] equal to [MouseEvent.MOUSE_DRAGGED] (when [tabDragged] is called)
     */
    @Suppress("PropertyName")
    protected open var _draggedTabAndIndex: OreTabAndIndex? = null
        set(new) {
            if (field != new) {
                field = new
            }
        }

    /**
     * The tab and its index for which the drag start event occurred
     *
     * _Note: for various reasons, the property may not become `null` if the Drag-and-Drop process has not started or has already ended_
     *
     * Filled before processing the [MouseEvent] event with [id][MouseEvent.id] equal to [MouseEvent.MOUSE_DRAGGED]
     */
    val draggedTabAndIndex: OreTabAndIndex?
        get() = this._draggedTabAndIndex

    /**
     * If set to `true` and [getTransferHandler] returns a not-null handler, then a Drag-and-Drop process will be started at the start of a tab drag
     * by calling [exportAsDrag][TransferHandler.exportAsDrag] of that transfer handler
     *
     * @see useDndImageRendererOnTabDragged
     */
    var useTransferHandlerOnTabDragged: Boolean = false

    /**
     * If set to `true`, then when the Drag-and-Drop process starts, the dragged tab image ([getTabImage]) will be set to [OreDnDImageRender.image]
     *
     * Starting a Drag-and-Drop process is described for [useTransferHandlerOnTabDragged]
     */
    var useDndImageRendererOnTabDragged: Boolean = false


    /**
     * Called when the Drag-and-Drop process ends (at [OreTabTransferHandler][me.ore.swing.ext.tabs.dnd.OreTabTransferHandler])
     *
     * When called, the [draggedTabAndIndex] property is cleared and the drawn drop location is removed
     */
    open fun endDragProcess() {
        this._draggedTabAndIndex = null
        this.paneUI.clearDropLocation(this.pane)
    }

    /**
     * Called to remove the drawn drop location
     */
    fun clearDropLocation() {
        this.paneUI.clearDropLocation(this.pane)
    }

    /**
     * Called to draw or update the drop location
     */
    fun paintDropLocation(dropPaintData: OreTabDropPaintData) {
        val draggedIndex = if (dropPaintData.sourcePane == this) {
            dropPaintData.sourceIndex
        } else {
            null
        }

        this.paneUI.paintDropLocation(this.pane, draggedIndex, dropPaintData.targetIndex)
    }
    // endregion


    // region Property proxy - tab placement
    /**
     * Tab placement
     *
     * @param swingValue Value for [JTabbedPane.tabPlacement]
     */
    enum class TabPlacement(val swingValue: Int) {
        /**
         * Tabs are at the top
         */
        @Suppress("unused")
        TOP(JTabbedPane.TOP),

        /**
         * Tabs are on the left
         */
        @Suppress("unused")
        LEFT(JTabbedPane.LEFT),

        /**
         * Tabs are at the bottom
         */
        @Suppress("unused")
        BOTTOM(JTabbedPane.BOTTOM),

        /**
         * Tabs are on the right
         */
        @Suppress("unused")
        RIGHT(JTabbedPane.RIGHT)
        ;

        companion object {
            /**
             * Searching a [TabPlacement] value that has [swingValue] equal to [value]
             *
             * @param value Required value for [TabPlacement.swingValue]
             *
             * @return [TabPlacement] value
             *
             * @throws IllegalArgumentException If no [TabPlacement] value is found that has [swingValue] equal to [value]
             */
            fun findBySwingValue(value: Int): TabPlacement {
                for (tabPlacement in values()) {
                    if (tabPlacement.swingValue == value) {
                        return tabPlacement
                    }
                }

                throw IllegalArgumentException("Cannot find ${TabPlacement::class.java.simpleName} with swing value $value")
            }
        }
    }

    /**
     * Tab placement (analogue of [JTabbedPane.tabPlacement][JTabbedPane.getTabPlacement])
     */
    var tabPlacement: TabPlacement
        get() = TabPlacement.findBySwingValue(this.pane.tabPlacement)
        set(new) { this.pane.tabPlacement = new.swingValue }
    // endregion


    // region Property proxy - tab layout policy
    /**
     * Tab layout policies on "overflow"
     *
     * @param swingValue Value for [JTabbedPane.tabLayoutPolicy]
     */
    enum class TabLayoutPolicy(val swingValue: Int) {
        /**
         * When "overflowing" the tabs are arranged in several lines
         */
        @Suppress("unused")
        WRAP(JTabbedPane.WRAP_TAB_LAYOUT),

        /**
         * When overflowing, the tabs are hidden outside the panel border, tab scroll buttons appear
         */
        @Suppress("unused")
        SCROLL(JTabbedPane.SCROLL_TAB_LAYOUT)
        ;

        companion object {
            /**
             * Search for [TabLayoutPolicy] that has [swingValue] equal to [value]
             *
             * @param value Required value for [TabLayoutPolicy.swingValue]
             *
             * @return [TabLayoutPolicy] value
             *
             * @throws IllegalArgumentException If no [TabLayoutPolicy] value is found that has [swingValue] equal to [value]
             */
            fun findBySwingValue(value: Int): TabLayoutPolicy {
                for (tabLayoutPolicy in values()) {
                    if (tabLayoutPolicy.swingValue == value) {
                        return tabLayoutPolicy
                    }
                }

                throw IllegalArgumentException("Cannot find ${TabLayoutPolicy::class.java.simpleName} with swing value $value")
            }
        }
    }

    /**
     * Tab layout policy when "overflowing" (analogue of [JTabbedPane.tabLayoutPolicy][JTabbedPane.getTabLayoutPolicy])
     */
    var tabLayoutPolicy: TabLayoutPolicy
        get() = TabLayoutPolicy.findBySwingValue(this.pane.tabLayoutPolicy)
        set(new) { this.pane.tabLayoutPolicy = new.swingValue }
    // endregion


    /**
     * Listener for [pane]'s property change events
     */
    private class PanePropertyChangeListener(pane: OreTabPane): PropertyChangeListener {
        /**
         * Weak reference to panel
         */
        private val paneReference = WeakReference(pane)

        /**
         * Default event handling
         *
         * If [paneReference].[get][WeakReference.get] returns `null` removes the listener from [pane][OreTabPane.pane];
         * otherwise - calls [block] with [event] passed and panel received
         *
         * @param event [pane]'s property change event
         * @param block Executable block
         */
        private inline fun defaultRun(event: PropertyChangeEvent?, block: (event: PropertyChangeEvent, pane: OreTabPane) -> Unit) {
            event
                ?: return

            val tabbedPane = (event.source as? JTabbedPane)
                ?: return

            val pane = this.paneReference.get()
            if (pane == null) {
                tabbedPane.removePropertyChangeListener(this)
            } else {
                try {
                    block(event, pane)
                } catch (e: Exception) {
                    OreSwingExt.handle(e)
                }
            }
        }

        /**
         * If [event.oldValue][PropertyChangeEvent.oldValue] and [event.newValue][PropertyChangeEvent.newValue] are [Int],
         * [translate] is called for each and the resulting values are passed to [pane].[firePropertyChange][OreTabPane.firePropertyChange]
         *
         * @param event Property change event
         * @param pane The panel for which to fire the property change event
         * @param translate Method to get values based on [event.oldValue][PropertyChangeEvent.oldValue] and [event.newValue][PropertyChangeEvent.newValue]
         */
        private inline fun <T> fireIntTranslatedProperty(event: PropertyChangeEvent, pane: OreTabPane, translate: (source: Int) -> T) {
            val oldInt = event.oldValue as? Int
                ?: return
            val newInt = event.newValue as? Int
                ?: return

            val old = translate(oldInt)
            val new = translate(newInt)

            pane.firePropertyChange(event.propertyName, old, new)
        }

        override fun propertyChange(propertyChangeEvent: PropertyChangeEvent?) {
            this.defaultRun(propertyChangeEvent) { event, pane ->
                when (event.propertyName) {
                    PROPERTY__TAB_PLACEMENT -> this.fireIntTranslatedProperty(event, pane) { TabPlacement.findBySwingValue(it) }
                    PROPERTY__TAB_LAYOUT_POLICY -> this.fireIntTranslatedProperty(event, pane) { TabLayoutPolicy.findBySwingValue(it) }
                    "model" -> {
                        (event.oldValue as? SingleSelectionModel)?.removeChangeListener(pane.paneModelChangeListener)
                        (event.newValue as? SingleSelectionModel)?.addChangeListener(pane.paneModelChangeListener)
                    }
                }
            }
        }
    }


    // region Pane model change listener
    /**
     * Change listener for [pane].[model][JTabbedPane.model]
     * (listening changes of [pane].[model][JTabbedPane.model].[selectedIndex][SingleSelectionModel.getSelectedIndex])
     */
    private class PaneModelChangeListener(pane: OreTabPane): ChangeListener {
        companion object {
            /**
             * Get the next value for [id], optionally different from [old] (if [old] is not equal to `null`)
             *
             * @param old Previous value of [id]
             *
             * @return New (next) value for [id]
             */
            fun nextId(old: Long?): Long {
                return when (old) {
                    null -> Long.MIN_VALUE
                    Long.MAX_VALUE -> Long.MIN_VALUE
                    else -> old + 1
                }
            }
        }

        /**
         * Weak reference to panel
         */
        private val paneReference = WeakReference(pane)

        /**
         * "ID" for change event of [pane].[model][JTabbedPane.model].[selectedIndex][SingleSelectionModel.getSelectedIndex]
         *
         * Used to reduce the number of handling of such events
         */
        private var id: Long? = null

        override fun stateChanged(event: ChangeEvent?) {
            val pane = this.paneReference.get()
            if (pane == null) {
                (event?.source as? SingleSelectionModel)?.removeChangeListener(this)
                return
            }

            val id = nextId(this.id)
            this.id = id

            EventQueue.invokeLater {
                if (this.id == id) {
                    this.id = null
                } else {
                    return@invokeLater
                }

                val index = pane.pane.model.selectedIndex
                if ((index >= 0) && (index < pane.model.tabs.size)) {
                    pane.model.select(index)
                }
            }
        }
    }

    /**
     * Listener for events that occur when the selected tab in [pane].[model][JTabbedPane.model] changes
     */
    private val paneModelChangeListener = PaneModelChangeListener(this)
    // endregion


    init {
        this.add(this.layer, BorderLayout.CENTER)

        this.pane.model.addChangeListener(this.paneModelChangeListener)

        this.pane.model = this.pane.model

        this.pane.addPropertyChangeListener(PanePropertyChangeListener(this))

        this.paneUI.addListener(this.uiListener)

        this.applyNewModel(this.model)
    }
}
