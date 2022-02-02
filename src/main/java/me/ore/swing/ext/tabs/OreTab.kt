package me.ore.swing.ext.tabs

import java.awt.Component
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport


/**
 * Tab for [OreTabPane]
 *
 * Objects of this class must be used in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
 *
 * @param content Tab content
 * @param title Tab title
 * @param closeable If `true`, the tab will display a close button
 */
open class OreTab(
    content: Component? = null,
    title: Component? = null,
    closeable: Boolean = false
) {
    companion object {
        /**
         * Property name for [content]
         *
         * Passed to [PropertyChangeListener]
         */
        const val PROPERTY__CONTENT = "content"

        /**
         * Property name for [title]
         *
         * Passed to [PropertyChangeListener]
         */
        const val PROPERTY__TITLE = "title"

        /**
         * Property name for [closeable]
         *
         * Passed to [PropertyChangeListener]
         */
        const val PROPERTY__CLOSEABLE = "closeable"

        /**
         * Property name for [pane]
         *
         * Passed to [PropertyChangeListener]
         */
        const val PROPERTY__PANE = "pane"
    }


    // region Property change support
    /**
     * Used to pass property change events to the [PropertyChangeListener]s
     */
    protected open val propertyChangeSupport: PropertyChangeSupport  by lazy(LazyThreadSafetyMode.NONE) { PropertyChangeSupport(this) }

    /**
     * Remove a [listener] from the listener list.
     * This removes a [listener] that was registered for all properties.
     * If [listener] was added more than once to the same event source, it will be notified one less time after being removed.
     * If [listener] was never added, no exception is thrown and no action is taken.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    @Suppress("unused")
    open fun removePropertyChangeListener(listener: PropertyChangeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(listener)
    }

    /**
     * Remove a [listener] for a specific property.
     * If [listener] was added more than once to the same event source for the specified property, it will be notified one less time after being removed.
     * If [listener] was never added for the specified property, no exception is thrown and no action is taken.
     *
     * @param propertyName The name of the property that was listened on.
     * @param listener The PropertyChangeListener to be removed
     */
    open fun removePropertyChangeListener(propertyName: String, listener: PropertyChangeListener) {
        this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener)
    }

    /**
     * Add a [listener] to the listener list.
     * The listener is registered for all properties.
     * The same listener object may be added more than once, and will be called as many times as it is added.
     *
     * @param listener The PropertyChangeListener to be added
     */
    @Suppress("unused")
    open fun addPropertyChangeListener(listener: PropertyChangeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(listener)
    }

    /**
     * Add a [listener] for a specific property.
     * The listener will be invoked only when a call on firePropertyChange names that specific property.
     * The same listener object may be added more than once.
     * For each property, the listener will be invoked the number of times it was added for that property.
     *
     * @param propertyName The name of the property to listen on
     * @param listener The PropertyChangeListener to be added
     */
    open fun addPropertyChangeListener(propertyName: String, listener: PropertyChangeListener) {
        this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener)
    }

    /**
     * Check if there are any listeners for a specific property, including those registered on all properties.
     * If [propertyName] is null, only check for listeners registered on all properties.
     *
     * @param propertyName The property name.
     *
     * @return `true` if there are one or more listeners for the given property
     */
    @Suppress("unused")
    open fun hasPropertyChangeListeners(propertyName: String?): Boolean {
        return this.propertyChangeSupport.hasListeners(propertyName)
    }

    /**
     * Returns an array of all the listeners that were added to this object with [addPropertyChangeListener].
     * If some listeners have been added with a named property, then the returned array will be a mixture of [PropertyChangeListener]s and
     * [PropertyChangeListenerProxy][java.beans.PropertyChangeListenerProxy]s.
     * If the calling method is interested in distinguishing the listeners then it must test each element to see
     * if it's a [PropertyChangeListenerProxy][java.beans.PropertyChangeListenerProxy], perform the cast, and examine the parameter.
     */
    open val propertyChangeListeners: Array<out PropertyChangeListener>
        get() = this.propertyChangeSupport.propertyChangeListeners

    /**
     * Returns an array of all the listeners which have been associated with the named property.
     *
     * @param propertyName The name of the property being listened to
     *
     * @return All of the [PropertyChangeListener]s associated with the named property. If no such listeners have been added, an empty array is returned.
     */
    @Suppress("unused")
    open fun getPropertyChangeListeners(propertyName: String): Array<out PropertyChangeListener> {
        return this.propertyChangeSupport.getPropertyChangeListeners(propertyName)
    }

    /**
     * Reports a bound property update to listeners that have been registered to track updates of all properties or a property with the specified name.
     *
     * No event is fired if [old] and [new] values are equal and non-null.
     *
     * @param propertyName The programmatic name of the property that was changed
     * @param old The old value of the property
     * @param new The new value of the property
     */
    protected open fun <T> firePropertyChange(propertyName: String, old: T, new: T) {
        this.propertyChangeSupport.firePropertyChange(propertyName, old, new)
    }
    // endregion


    /**
     * Tab content
     */
    open var content: Component? = content
        set(new) {
            if (field != new) {
                val old = field
                field = new
                this.firePropertyChange(PROPERTY__CONTENT, old, new)
            }
        }

    /**
     * Tab title
     */
    open var title: Component? = title
        set(new) {
            if (field != new) {
                val old = field
                field = new
                this.firePropertyChange(PROPERTY__TITLE, old, new)
            }
        }

    /**
     * If true, the tab will display a close button
     */
    open var closeable: Boolean = closeable
        set(new) {
            if (field != new) {
                val old = field
                field = new
                this.firePropertyChange(PROPERTY__CLOSEABLE, old, new)
            }
        }


    // region Pane
    /**
     * The panel to which the tab belongs; mutable version of [pane]
     */
    @Suppress("PropertyName")
    protected open var _pane: OreTabPane? = null
        set(new) {
            if (field != new) {
                val old = field
                field = new
                this.firePropertyChange(PROPERTY__PANE, old, new)
            }
        }

    /**
     * The panel to which the tab belongs
     */
    @Suppress("unused")
    open val pane: OreTabPane?
        get() = this._pane

    /**
     * Checking and updating [pane]
     */
    open fun updatePane() {
        this._pane = OreTabPane.getPaneForTab(this)
    }
    // endregion


    /**
     * Called when the user clicks on the close tab button
     *
     * In some situations, it is necessary to cancel the closing of the tab, for example, to display a dialog about unsaved data.
     * In this case, this method should return `false` (the tab will not be removed from the panel), the tab can be removed programmatically later
     * (for example, using this.[pane]?.[model][OreTabPane.model]?.[removeTab][OreTabModel.removeTab])
     *
     * @return `true` if the tab can be removed from the panel; returns value of [closeable] by default
     */
    open fun closeClicked(): Boolean {
        return (this.closeable)
    }
}
