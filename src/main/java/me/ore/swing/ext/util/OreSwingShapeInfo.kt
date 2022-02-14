package me.ore.swing.ext.util

import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.ceil


/**
 * The class of objects that store some [shape] and the bounds of the area ([viewBox]),
 * which is taken from `shape` when drawing
 *
 * @param viewBox Borders of the area that is taken from [shape] when drawing
 * @param shape Any shape
 */
open class OreSwingShapeInfo(
    open var viewBox: Rectangle2D,
    open var shape: Shape
) {
    /**
     * Creates shape info by reading the shape from `pathString` using [OreSwingSVGPathParser.parse]
     *
     * @param viewBox Borders of the area that is taken from [shape] when drawing
     * @param pathString SVG path definition (attribute "d" of element `<path>`), which will be parsed and saved to [shape]
     */
    constructor(viewBox: Rectangle2D, pathString: String): this(viewBox, OreSwingSVGPathParser.parse(pathString))


    /**
     * Changes the coordinates and sizes of the [viewBox] and [shape] objects
     *
     * Horizontal coordinates and dimensions (along the X-axis) are multiplied by [xFactor], vertical (along the Y-axis) - by [yFactor]
     *
     * @param xFactor Multiplier by which horizontal coordinates and dimensions are multiplied (along the X-axis)
     * @param yFactor Multiplier by which vertical coordinates and dimensions are multiplied (along the Y-axis)
     *
     * @return This info object
     */
    open fun scale(xFactor: Double, yFactor: Double): OreSwingShapeInfo {
        val viewBox = this.viewBox.let { source ->
            Rectangle2D.Double(
                source.x * xFactor,
                source.y * yFactor,
                source.width * xFactor,
                source.height * yFactor
            )
        }

        val shape = AffineTransform
            .getScaleInstance(xFactor, yFactor)
            .createTransformedShape(this.shape)

        return OreSwingShapeInfo(viewBox, shape)
    }

    /**
     * Changes the coordinates and sizes of the [viewBox] and [shape] objects
     *
     * Both horizontal (along the X-axis) and vertical (along the Y-axis) coordinates and dimensions are multiplied by [factor]
     *
     * By fact, calls `this.scale(factor, factor)`
     *
     * @param factor Multiplier by which all coordinates and dimensions are multiplied
     *
     * @return This info object
     */
    open fun scale(factor: Double): OreSwingShapeInfo = this.scale(factor, factor)


    /**
     * Drawing a [shape] as a [BufferedImage], the bounds of the usable area of the `shape` are defined with the [viewBox].
     *
     * The image size will be ceil(`viewBox`.[getWidth()][Rectangle2D.getWidth]) × ceil(`viewBox`.[getHeight()][Rectangle2D.getHeight]).
     * The point of the shape that will be the top left pixel in the image (at coordinates 0 × 0)
     * has coordinates `viewBox`.[getX()][Rectangle2D.getX] × `viewBox`.[getY()][Rectangle2D.getY].
     *
     * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shape; otherwise - [Graphics2D.draw] method
     * @param imageType Image type; must be one of the `BufferedImage.TYPE_*` constants
     * @param init The block in which the [Graphics2D] image object is prepared (see [BufferedImage.createGraphics]);
     * e.g. setting [color][Graphics2D.setColor]
     *
     * @return The image in which the `shape` is drawn
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inline fun toImage(fill: Boolean = true, imageType: Int = BufferedImage.TYPE_INT_ARGB, init: (graphics: Graphics2D) -> Unit): BufferedImage {
        contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }

        val width: Int
        val height: Int
        val translateX: Double
        val translateY: Double
        this.viewBox.let { viewBox ->
            width = ceil(viewBox.width).toInt()
            height = ceil(viewBox.height).toInt()
            translateX = 0 - viewBox.x
            translateY = 0 - viewBox.y
        }

        val result = BufferedImage(width, height, imageType)

        val graphics = result.createGraphics()
        try {
            init(graphics)

            val transform = if ((translateX != 0.0) || (translateY != 0.0)) {
                AffineTransform.getTranslateInstance(translateX, translateY)
            } else {
                null
            }

            val actualShape = transform?.createTransformedShape(shape) ?: shape
            if (fill) {
                graphics.fill(actualShape)
            } else {
                graphics.draw(actualShape)
            }
        } finally {
            graphics.dispose()
        }

        return result
    }

    /**
     * Drawing a [shape] as a [BufferedImage], the bounds of the usable area of the `shape` are defined with the [viewBox].
     *
     * The image size will be ceil(`viewBox`.[getWidth()][Rectangle2D.getWidth]) × ceil(`viewBox`.[getHeight()][Rectangle2D.getHeight]).
     * The point of the shape that will be the top left pixel in the image (at coordinates 0 × 0)
     * has coordinates `viewBox`.[getX()][Rectangle2D.getX] × `viewBox`.[getY()][Rectangle2D.getY].
     *
     * @param config Settings for the [Graphics2D] object used to draw the image
     * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shape; otherwise - [Graphics2D.draw] method
     * @param imageType Image type; must be one of the `BufferedImage.TYPE_*` constants
     *
     * @return The image in which the `shape` is drawn
     */
    fun toImage(config: OreGraphicsConfig, fill: Boolean = true, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
        return this.toImage(fill, imageType) { config.applyTo(it) }
    }
}
