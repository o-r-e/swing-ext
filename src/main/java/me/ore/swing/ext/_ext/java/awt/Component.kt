package me.ore.swing.ext._ext.java.awt

import java.awt.Component
import java.awt.Window


/**
 * Getting the window the component belongs to
 *
 * Method must be executed in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
 *
 * @return The window this component belongs to; `null` if the component does not belong to any window
 */
fun Component.getOreComponentWindow(): Window? {
    var current: Component? = this
    while ((current != null) && (current !is Window)) {
        current = current.parent
    }

    return (current as? Window)
}
