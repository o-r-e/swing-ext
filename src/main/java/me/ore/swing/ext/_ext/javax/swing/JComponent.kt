package me.ore.swing.ext._ext.javax.swing

import java.awt.Rectangle
import java.awt.image.BufferedImage
import javax.swing.JComponent
import kotlin.math.max
import kotlin.math.min


/**
 * Creates a component image
 *
 * The image will be the same size as the component
 *
 * _Note: components that are not yet rendered in some window can have zero width and/or height_
 *
 * Method must be executed in [EventQueue.dispatchThread][java.awt.EventQueue.dispatchThread]
 *
 * @param imageType Image type; must be one of the [BufferedImage].`TYPE_*` constants
 *
 * @return Component image
 */
fun JComponent.getOreComponentImage(imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
    val size = this.size
    val width = size.width
    val height = size.height
    if ((width == 0) || (height == 0))
        return BufferedImage(width, height, imageType)

    return BufferedImage(width, height, imageType).also { image ->
        val graphics = image.createGraphics()
            ?: error("Cannot create graphics for result image")

        try {
            this.paint(graphics)
        } finally {
            graphics.dispose()
        }
    }
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
 * @param bounds Bounds of the part of the component that needs to be included in the image
 * @param imageType Image type; must be one of the [BufferedImage].`TYPE_*` constants
 *
 * @return Image of a part of a component
 */
fun JComponent.getOreComponentImage(bounds: Rectangle, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
    val width = bounds.width
    val height = bounds.height
    if ((width == 0) || (height == 0))
        return BufferedImage(width, height, imageType)

    val fullImage = this.getOreComponentImage(imageType)

    val x = bounds.x
    val y = bounds.y

    val actualWidth = max(min(bounds.width, fullImage.width - x), 0)
    val actualHeight = max(min(bounds.height, fullImage.height - y), 0)
    if ((actualWidth == 0) || (actualHeight == 0))
        return BufferedImage(actualWidth, actualHeight, imageType)

    return BufferedImage(actualWidth, actualHeight, imageType).also { image ->
        val graphics = image.createGraphics()
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
    }
}
