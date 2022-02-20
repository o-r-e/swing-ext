@file:Suppress("unused")

package me.ore.swing.ext.util

import java.awt.Dimension
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.math.ceil


/**
 * An image template of a specific size ([width]x[height]) whose contents are [shapes]
 *
 * @param width Width of image template
 * @param height Height of image template
 * @param shapes Shapes that are image content
 */
open class OreSwingShapeTemplate(
    width: Double,
    height: Double,
    vararg shapes: Shape
) {
    companion object {
        /**
         * Recognizes each element in `pathDefinitions` as an SVG path definition using [OreSwingSVGPathParser.parse]
         *
         * @param pathDefinitions SVG path definitions to be read as [Shape]s
         *
         * @return Array of forms read from `pathDefinitions`
         */
        fun parse(pathDefinitions: Array<out String>): Array<Shape> {
            return Array(pathDefinitions.size) { i ->
                OreSwingSVGPathParser.parse(pathDefinitions[i])
            }
        }

        /**
         * Translates the coordinates of each element in `shapes` by `translateX` to the right and by `translateY` down
         *
         * If `translateX` and `translateY` are `0` or there are no elements in `shapes`, returns `shapes`;
         * otherwise - returns a new array with modified shapes
         *
         * @param translateX How much to shift the coordinates to the right
         * @param translateY How much to shift the coordinates to the down
         * @param shapes Array of forms to be modified
         *
         * @return Array with modified shapes
         */
        fun transform(translateX: Double, translateY: Double, shapes: Array<out Shape>): Array<out Shape> {
            if (((translateX == 0.0) && (translateY == 0.0)) || (shapes.isEmpty()))
                return shapes

            return Array(shapes.size) { i ->
                val shape = shapes[i]
                val transform = AffineTransform.getTranslateInstance(translateX, translateY)
                transform.createTransformedShape(shape)
            }
        }
    }


    // region Constructors
    /**
     * Creates an image template of the specified size based on the SVG path definitions in `pathDefinitions`
     *
     * Each element in `pathDefinitions` is converted to a [Shape] using [OreSwingSVGPathParser.parse]
     *
     * @param width Width of image template
     * @param height Height of image template
     * @param pathDefinitions SVG path definitions to be used as image content
     */
    constructor(width: Double, height: Double, vararg pathDefinitions: String):
            this(width, height, *parse(pathDefinitions))

    /**
     * Creates an image template given the bounds of the "display area" relative to the shapes in `shapes`
     *
     * The [shapes][OreSwingShapeTemplate.shapes] property stores copies of the elements of the [shapes] array
     * whose coordinates are shifted by `viewBox.`[x][Rectangle2D.getX] to the left and `viewBox.`[y][Rectangle2D.getY] up
     *
     * The width of the image template will be `viewBox.`[width][Rectangle2D.getWidth],
     * the height will be `viewBox.`[height][Rectangle2D.getHeight]
     *
     * @param viewBox Bounds of the "display area" relative to the elements in `shapes`
     * @param shapes Shapes to be used as image content
     */
    constructor(viewBox: Rectangle2D, vararg shapes: Shape):
            this(viewBox.width, viewBox.height, *transform(0 - viewBox.x, 0 - viewBox.y, shapes))

    /**
     * Creates an image template given the bounds of the "display area" relative to the shapes to be read from `pathDefinitions`
     *
     * Each element in `pathDefinitions` is converted to a [Shape] using [OreSwingSVGPathParser.parse]
     * with coordinates are shifted by `viewBox.`[x][Rectangle2D.getX] to the left and `viewBox.`[y][Rectangle2D.getY] up
     *
     * The width of the image template will be `viewBox.`[width][Rectangle2D.getWidth],
     * the height will be `viewBox.`[height][Rectangle2D.getHeight]
     *
     * @param viewBox Bounds of the "display area" relative to the shapes to be read from `pathDefinitions`
     * @param pathDefinitions SVG path definitions to be used as image content
     */
    constructor(viewBox: Rectangle2D, vararg pathDefinitions: String):
            this(viewBox, *parse(pathDefinitions))
    // endregion


    // region Size
    /**
     * Method for preparing value for property [_width] during object initialization
     *
     * Returns `width` by default, can be overridden
     *
     * @param width Initial value
     *
     * @return Prepared value
     */
    protected open fun prepareWidth(width: Double): Double = width

    /**
     * Image template width
     *
     * Modifiable version of the [width] property
     */
    @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
    protected var _width: Double = this.prepareWidth(width)

    /**
     * Image template width
     */
    open val width: Double
        get() = this._width

    /**
     * Width of the image template, specified as an integer
     *
     * Default is the smallest integer greater than or equal to [width]
     */
    open val intWidth: Int
        get() = ceil(this.width).toInt()


    /**
     * Method for preparing value for property [_height] during object initialization
     *
     * Returns `height` by default, can be overridden
     *
     * @param height Initial value
     *
     * @return Prepared value
     */
    protected open fun prepareHeight(height: Double): Double = height

    /**
     * Image template height
     *
     * Modifiable version of the [height] property
     */
    @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
    protected val _height: Double = this.prepareHeight(height)

    /**
     * Image template height
     */
    open val height: Double
        get() = this._height

    /**
     * Height of the image template, specified as an integer
     *
     * Default is the smallest integer greater than or equal to [height]
     */
    open val intHeight: Int
        get() = ceil(this.height).toInt()


    /**
     * Image template size
     *
     * By default - `Dimension(this.intWidth, this.intHeight)`
     */
    open val size: Dimension
        get() = Dimension(this.intWidth, this.intHeight)
    // endregion


    // region Shapes
    /**
     * Method for preparing the content to be stored in the [_shapes] property during object initialization
     *
     * By default - simply returns `shapes`
     *
     * @param shapes Array of shapes passed to the constructor
     *
     * @return Array of prepared shapes
     */
    protected open fun prepareShapes(shapes: Array<out Shape>): Array<out Shape> = shapes

    /**
     * An array of shapes that are the content of the image
     *
     * Modifiable version of the [shapes] property
     */
    @Suppress("MemberVisibilityCanBePrivate", "PropertyName")
    protected var _shapes: Array<out Shape> = this.prepareShapes(shapes)

    /**
     * An array of shapes that are the content of the image
     */
    open val shapes: Array<out Shape>
        get() {
            val shapes = this._shapes
            return Array(shapes.size) { i -> Path2D.Double(shapes[i]) }
        }
    // endregion


    // region Scale
    /**
     * Returns a copy of the current image template with all dimensions and coordinates scaled.
     * All dimensions and coordinates along the X-axis are multiplied by `xFactor`, along the Y-axis - by `yFactor`.
     *
     * @param xFactor Multiplier by which horizontal coordinates and dimensions are multiplied (along the X-axis)
     * @param yFactor Multiplier by which vertical coordinates and dimensions are multiplied (along the Y-axis)
     *
     * @return New image template
     */
    open fun scale(xFactor: Double, yFactor: Double): OreSwingShapeTemplate {
        if ((xFactor == 1.0) && (yFactor == 1.0))
            return OreSwingShapeTemplate(this.width, this.height, *this.shapes)

        val width = (this.width * xFactor)
        val height = (this.height * yFactor)

        val transform = AffineTransform.getScaleInstance(xFactor, yFactor)
        val originalShapes = this.shapes
        val shapes = Array(originalShapes.size) { i ->
            transform.createTransformedShape(originalShapes[i])
        }

        return OreSwingShapeTemplate(width, height, *shapes)
    }

    /**
     * Returns a copy of the current image template with all dimensions and coordinates scaled.
     * All dimensions and coordinates are multiplied by `factor`.
     *
     * By fact, calls `this.scale(factor, factor)`
     *
     * @param factor Multiplier by which all coordinates and dimensions are multiplied
     *
     * @return New image template
     */
    open fun scale(factor: Double): OreSwingShapeTemplate = this.scale(factor, factor)
    // endregion


    // region Drawing
    /**
     * Drawing a [shapes] as a [BufferedImage].
     *
     * The image size will be [intWidth]×[intHeight].
     *
     * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shapes; otherwise - [Graphics2D.draw] method
     * @param imageType Image type; must be one of the `BufferedImage.TYPE_*` constants
     * @param init The block in which the [Graphics2D] image object is prepared (see [BufferedImage.createGraphics]);
     * e.g. setting [color][Graphics2D.setColor]
     *
     * @return The image in which the `shapes` is drawn
     */
    @Suppress("MemberVisibilityCanBePrivate")
    inline fun toImage(fill: Boolean = true, imageType: Int = BufferedImage.TYPE_INT_ARGB, init: (graphics: Graphics2D) -> Unit): BufferedImage {
        contract { callsInPlace(init, InvocationKind.EXACTLY_ONCE) }

        val width = this.intWidth
        val height = this.intHeight
        val shapes = this.shapes

        val result = BufferedImage(width, height, imageType)

        val graphics = result.createGraphics()
        try {
            init(graphics)
            if (fill) {
                shapes.forEach { shape -> graphics.fill(shape) }
            } else {
                shapes.forEach { shape -> graphics.draw(shape) }
            }
        } finally {
            graphics.dispose()
        }

        return result
    }

    /**
     * Drawing a [shapes] as a [BufferedImage].
     *
     * The image size will be [intWidth]×[intHeight].
     *
     * @param config Settings for the [Graphics2D] object used to draw the image
     * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shapes; otherwise - [Graphics2D.draw] method
     * @param imageType Image type; must be one of the `BufferedImage.TYPE_*` constants
     *
     * @return The image in which the `shapes` is drawn
     */
    fun toImage(config: OreGraphicsConfig, fill: Boolean = true, imageType: Int = BufferedImage.TYPE_INT_ARGB): BufferedImage {
        return this.toImage(fill, imageType) { config.applyTo(it) }
    }
    // endregion
}


/**
 * Draws [shapes][OreSwingShapeTemplate.shapes] using `graphics` starting at point 0x0
 *
 * @param T Current type (class) of image template
 * @param graphics The graphic object in which the current image template will be drawn;
 * __object must be prepared__ (e.g. color already set, etc.)
 * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shapes; otherwise - [Graphics2D.draw] method
 *
 * @return This image template
 */
fun <T: OreSwingShapeTemplate> T.drawTo(graphics: Graphics2D, fill: Boolean = true): T = this.apply {
    if (fill) {
        this.shapes.forEach { shape -> graphics.fill(shape) }
    } else {
        this.shapes.forEach { shape -> graphics.draw(shape) }
    }
}

/**
 * Draws [shapes][OreSwingShapeTemplate.shapes] into `image` starting at point 0x0
 *
 * @param T Current type (class) of image template
 * @param image The image where the current template will be drawn
 * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shapes; otherwise - [Graphics2D.draw] method
 * @param init The block in which the [Graphics2D] image object is prepared (see [BufferedImage.createGraphics]);
 * e.g. setting [color][Graphics2D.setColor]
 *
 * @return This image template
 */
inline fun <T: OreSwingShapeTemplate> T.drawTo(image: BufferedImage, fill: Boolean = true, init: (graphics: Graphics2D) -> Unit): T = this.apply {
    val graphics = image.createGraphics()
    try {
        init(graphics)
        this.drawTo(graphics, fill)
    } finally {
        graphics.dispose()
    }
}

/**
 * Draws [shapes][OreSwingShapeTemplate.shapes] into `image` starting at point 0x0
 *
 * @param T Current type (class) of image template
 * @param image The image where the current template will be drawn
 * @param config Settings for the [Graphics2D] object used to draw the image
 * @param fill If set to `true`, the [Graphics2D.fill] method will be used to draw the shapes; otherwise - [Graphics2D.draw] method
 *
 * @return This image template
 */
fun <T: OreSwingShapeTemplate> T.drawTo(image: BufferedImage, config: OreGraphicsConfig, fill: Boolean = true): T =
    this.drawTo(image, fill) { graphics ->  config.applyTo(graphics) }
