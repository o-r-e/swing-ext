package me.ore.swing.ext.tabs

import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.awt.Dimension
import javax.swing.Box
import javax.swing.JPanel


/**
 * Tab title component
 *
 * @param closeButtonSize The size of the area reserved for the close button
 * @param closeable If equal to `true`, then the area reserved for the close button is added to the component
 * @param title Component's content
 */
class OreTabTitle(closeButtonSize: Dimension, closeable: Boolean, title: Component?): JPanel(BorderLayout()) {
    companion object {
        /**
         * Name for property [closeButtonSize]
         *
         * Used in [PropertyChangeEvent][java.beans.PropertyChangeEvent] events
         */
        const val PROPERTY__CLOSE_BUTTON_SIZE = "closeButtonSize"

        /**
         * Name for property [closeable]
         *
         * Used in [PropertyChangeEvent][java.beans.PropertyChangeEvent] events
         */
        const val PROPERTY__CLOSEABLE = "closeable"

        /**
         * Name for property [title]
         *
         * Used in [PropertyChangeEvent][java.beans.PropertyChangeEvent] events
         */
        const val PROPERTY__TITLE = "title"


        /**
         * Transparent color, used as background color [OreTabTitle]
         */
        private val TRANSPARENT = Color(0, 0, 0, 0)


        /**
         * Creating a tab title based on additional UI layer data and tab data
         *
         * Data used: [paneUI].[closeButtonPlaceSize][OreTabPaneUI.closeButtonPlaceSize], [tab].[closeable][OreTab.closeable] and [tab].[title][OreTab.title]
         *
         * @param paneUI Additional UI layer
         * @param tab Tab
         *
         * @return Tab title component
         */
        fun create(paneUI: OreTabPaneUI, tab: OreTab): OreTabTitle = OreTabTitle(paneUI.closeButtonPlaceSize, tab.closeable, tab.title)
    }


    /**
     * Area reserved for close button
     */
    private var closeButtonPlaceholder: Component = Box.createRigidArea(closeButtonSize)


    /**
     * The size of the area reserved for the close button
     */
    var closeButtonSize: Dimension = closeButtonSize
        set(new) {
            if (field != new) {
                val old = field
                field = new

                if (this.closeable) {
                    this.remove(this.closeButtonPlaceholder)
                }
                this.closeButtonPlaceholder = Box.createRigidArea(new)
                if (this.closeable) {
                    this.add(this.closeButtonPlaceholder, BorderLayout.EAST)
                    this.revalidate()
                }

                this.firePropertyChange(PROPERTY__CLOSE_BUTTON_SIZE, old, new)
            }
        }

    /**
     * If equal to `true`, then the area reserved for the close button is added to the component
     */
    var closeable: Boolean = closeable
        set(new) {
            if (field != new) {
                val old = field
                field = new

                if (new) {
                    this.add(this.closeButtonPlaceholder, BorderLayout.EAST)
                } else {
                    this.remove(this.closeButtonPlaceholder)
                }

                this.revalidate()

                this.firePropertyChange(PROPERTY__CLOSEABLE, old, new)
            }
        }

    /**
     * Component's content
     */
    var title: Component? = title
        set(new) {
            if (field != new) {
                val old = field
                field = new

                var revalidate = false
                old?.let {
                    this.remove(old)
                    revalidate = true
                }
                new?.let {
                    this.add(new, BorderLayout.CENTER)
                    revalidate = true
                }
                if (revalidate) {
                    this.revalidate()
                }

                this.firePropertyChange(PROPERTY__TITLE, old, new)
            }
        }


    init {
        this.background = TRANSPARENT
        if (this.closeable) {
            this.add(this.closeButtonPlaceholder, BorderLayout.EAST)
        }
        this.title?.let {
            this.add(it, BorderLayout.CENTER)
        }
    }
}
