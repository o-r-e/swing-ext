package me.ore.swing.ext.util

import me.ore.swing.ext.OreSwingExt
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeListener
import java.beans.PropertyChangeSupport


/**
 * Interface for classes which use [PropertyChangeSupport][java.beans.PropertyChangeSupport] inside
 *
 * _Descriptions of methods and properties are copied from relevant methods and properties of [PropertyChangeSupport][java.beans.PropertyChangeSupport]_
 */
@Suppress("unused")
interface OrePropertyChangeEmitter<L: PropertyChangeListener> {
    /**
     * Returns an array of all the listeners that were added to this object with [addPropertyChangeListener].
     *
     * If some listeners have been added with a named property, then the returned array will be a mixture of [PropertyChangeListener]s and
     * [PropertyChangeListenerProxy][java.beans.PropertyChangeListenerProxy]s.
     * If the calling method is interested in distinguishing the listeners
     * then it must test each element to see if it's a [PropertyChangeListenerProxy][java.beans.PropertyChangeListenerProxy],
     * perform the cast, and examine the parameter.
     */
    val propertyChangeListeners: List<L>

    /**
     * Returns an array of all the listeners which have been associated with the named property
     *
     * @param propertyName The name of the property being listened to
     *
     * @return All of the [PropertyChangeListener]s associated with the named property.
     * If no such listeners have been added, an empty array is returned.
     */
    fun getPropertyChangeListeners(propertyName: String): List<L>

    /**
     * Check if there are any listeners for a specific property, including those registered on all properties.
     * If [propertyName] is null, only check for listeners registered on all properties.
     *
     * @param propertyName The property name
     *
     * @return `true` if there are one or more listeners for the given property
     */
    fun hasListeners(propertyName: String?): Boolean


    /**
     * Add a PropertyChangeListener to the listener list.
     * The listener is registered for all properties.
     * The same listener object may be added more than once, and will be called as many times as it is added.
     *
     * @param listener The PropertyChangeListener to be added
     */
    fun addPropertyChangeListener(listener: L)

    /**
     * Add a PropertyChangeListener for a specific property.
     * The [listener] will be invoked only when a that specific property changes.
     * The same listener object may be added more than once.
     * For each property, the listener will be invoked the number of times it was added for that property.
     *
     * @param propertyName The name of the property to listen on
     * @param listener The PropertyChangeListener to be added
     */
    fun addPropertyChangeListener(propertyName: String, listener: L)


    /**
     * Remove a PropertyChangeListener from the listener list.
     * This removes a PropertyChangeListener that was registered for all properties.
     * If [listener] was added more than once to the same event source, it will be notified one less time after being removed.
     *
     * @param listener The PropertyChangeListener to be removed
     */
    fun removePropertyChangeListener(listener: L)

    /**
     * Remove a PropertyChangeListener for a specific property.
     * If [listener] was added more than once to the same event source for the specified property,
     * it will be notified one less time after being removed.
     * If [listener] was never added for the specified property, no exception is thrown and no action is taken.
     *
     * @param propertyName The name of the property that was listened on
     * @param listener The PropertyChangeListener to be removed
     */
    fun removePropertyChangeListener(propertyName: String, listener: L)


    @Suppress("UNCHECKED_CAST")
    open class Impl<L: PropertyChangeListener>: OrePropertyChangeEmitter<L> {
        @Suppress("LeakingThis")
        protected open val propertyChangeSupport: PropertyChangeSupport = PropertyChangeSupport(this)


        override val propertyChangeListeners: List<L>
            get() = this.propertyChangeSupport.propertyChangeListeners.toList() as List<L>

        override fun getPropertyChangeListeners(propertyName: String): List<L> =
            this.propertyChangeSupport.getPropertyChangeListeners(propertyName).toList() as List<L>

        override fun hasListeners(propertyName: String?): Boolean = this.propertyChangeSupport.hasListeners(propertyName)

        override fun addPropertyChangeListener(listener: L) {
            this.propertyChangeSupport.addPropertyChangeListener(listener)
        }

        override fun addPropertyChangeListener(propertyName: String, listener: L) {
            this.propertyChangeSupport.addPropertyChangeListener(propertyName, listener)
        }

        override fun removePropertyChangeListener(listener: L) {
            this.propertyChangeSupport.removePropertyChangeListener(listener)
        }

        override fun removePropertyChangeListener(propertyName: String, listener: L) {
            this.propertyChangeSupport.removePropertyChangeListener(propertyName, listener)
        }


        protected open fun firePropertyChange(propertyName: String, oldValue: Any?, newValue: Any?) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue)
        }

        protected open fun firePropertyChange(propertyName: String, oldValue: Int, newValue: Int) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue)
        }

        protected open fun firePropertyChange(propertyName: String, oldValue: Boolean, newValue: Boolean) {
            this.propertyChangeSupport.firePropertyChange(propertyName, oldValue, newValue)
        }

        protected open fun firePropertyChange(event: PropertyChangeEvent) {
            this.propertyChangeSupport.firePropertyChange(event)
        }

        protected open fun fireIndexedPropertyChange(propertyName: String, index: Int, oldValue: Any?, newValue: Any?) {
            this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue)
        }

        protected open fun fireIndexedPropertyChange(propertyName: String, index: Int, oldValue: Int, newValue: Int) {
            this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue)
        }

        protected open fun fireIndexedPropertyChange(propertyName: String, index: Int, oldValue: Boolean, newValue: Boolean) {
            this.propertyChangeSupport.fireIndexedPropertyChange(propertyName, index, oldValue, newValue)
        }

        inline fun forEachListener(block: (listener: L) -> Unit) {
            this.propertyChangeListeners.forEach { listener ->
                try {
                    block(listener)
                } catch (e: Exception) {
                    OreSwingExt.handle(e)
                }
            }
        }
    }
}
