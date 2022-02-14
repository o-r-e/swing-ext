package me.ore.swing.ext

import java.awt.Component
import java.awt.Rectangle
import java.awt.Window
import java.awt.image.BufferedImage
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.min


/**
 * Miscellaneous utility methods
 */
object OreSwingExt {
    /**
     * Error handling
     *
     * Tries to handle the error by iterating over the options:
     * * error handling with [Thread.uncaughtExceptionHandler] of the current thread
     * * error handling with [Thread.defaultUncaughtExceptionHandler]
     * * error output with [Throwable.printStackTrace] ([System.err])
     *
     * After the first variant that worked, it aborts the execution
     *
     * This method is thread-safe
     *
     * @param exception Error to handle
     */
    fun handle(exception: Exception) {
        val thread = Thread.currentThread()
        var processed = false

        // region Use handler of current thread
        try {
            thread.uncaughtExceptionHandler?.let {
                it.uncaughtException(thread, exception)
                processed = true
            }
        } catch (e: Exception) {
            exception.addSuppressed(e)
        }
        // endregion
        if (processed) return

        // region Use default handler
        try {
            Thread.getDefaultUncaughtExceptionHandler()?.let {
                it.uncaughtException(thread, exception)
                processed = true
            }
        } catch (e: Exception) {
            exception.addSuppressed(e)
        }
        // endregion
        if (processed) return

        // Write to console
        exception.printStackTrace(System.err)
    }


    /**
     * Getting the window the component belongs to
     *
     * Method must be executed in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
     *
     * @param component The component to get the window for
     *
     * @return The window this component belongs to; `null` if the component does not belong to any window
     */
    fun getWindowOf(component: Component): Window? {
        var current: Component? = component
        while (current != null) {
            if (current is Window) {
                return current
            }
            current = current.parent
        }

        return null
    }


    /**
     * Creates a component image
     *
     * The image will be the same size as the component
     *
     * _Note: components that are not yet rendered in some window can have zero width and/or height_
     *
     * Method must be executed in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
     *
     * @param component The component to get the image for
     * @param imageType Image type; must be one of the [BufferedImage].`TYPE_*` constants
     *
     * @return Component image
     */
    @Suppress("MemberVisibilityCanBePrivate")
    fun getImageOf(component: JComponent, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
        val size = component.size
        val width = size.width
        val height = size.height
        if ((width == 0) || (height == 0))
            return BufferedImage(width, height, imageType)

        val result = BufferedImage(width, height, imageType)
        val graphics = result.createGraphics()
            ?: error("Cannot create graphics for result image")

        try {
            component.paint(graphics)
        } finally {
            graphics.dispose()
        }

        return result
    }

    /**
     * Create an image of a part of a component
     *
     * The image will have the size and content of the "intersection" of the component's dimensions and [bounds],
     * i.e. image size and content may be smaller than [bounds] if [bounds] is outside the component's bounds
     *
     * _Note: components that are not yet rendered in some window can have zero width and/or height_
     *
     * Method must be executed in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
     *
     * @param component The component to get the image for
     * @param bounds Bounds of the part of the component that needs to be included in the image
     * @param imageType Image type; must be one of the [BufferedImage].`TYPE_*` constants
     *
     * @return Image of a part of a component
     */
    fun getImageOf(component: JComponent, bounds: Rectangle, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
        val width = bounds.width
        val height = bounds.height
        if ((width == 0) || (height == 0))
            return BufferedImage(width, height, imageType)

        val fullImage = this.getImageOf(component, imageType)

        val x = bounds.x
        val y = bounds.y

        val actualWidth = max(min(bounds.width, fullImage.width - x), 0)
        val actualHeight = max(min(bounds.height, fullImage.height - y), 0)
        if ((actualWidth == 0) || (actualHeight == 0))
            return BufferedImage(actualWidth, actualHeight, imageType)

        val result = BufferedImage(actualWidth, actualHeight, imageType)
        val graphics = result.createGraphics()
            ?: error("Cannot create graphics for result image")

        try {
            graphics.drawImage(
                fullImage,
                0, 0, actualWidth, actualHeight,
                x, y, x + actualWidth, y + actualHeight,
                null
            )
        } finally {
            graphics.dispose()
        }

        return result
    }


    /**
     * TODO doc
     */
    @Suppress("unused")
    inline fun <T> printTime(logPrefix: String? = null, logSuffix: String? = null, block: () -> T): T {
        var time = System.currentTimeMillis()

        val result = block()

        time = System.currentTimeMillis() - time
        println(buildString {
            if (logPrefix != null)
                this.append(logPrefix)

            val firstWord = if (logPrefix.isNullOrBlank()) { "Elapsed" } else { "elapsed" }

            this
                .append(firstWord)
                .append(" time - ")
                .append(time)
                .append(" ms")

            if (logSuffix != null)
                this.append(logSuffix)
        })

        return result
    }
}
