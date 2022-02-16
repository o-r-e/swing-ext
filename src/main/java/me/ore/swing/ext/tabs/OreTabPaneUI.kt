package me.ore.swing.ext.tabs

import me.ore.swing.ext.OreSwingExt
import me.ore.swing.ext.util.OreGraphicsConfig
import me.ore.swing.ext.util.OreSwingShapeInfo
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JComponent
import javax.swing.JLayer
import javax.swing.JTabbedPane
import javax.swing.UIManager
import javax.swing.plaf.LayerUI
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


/**
 * Additional UI layer for [OreTabPane]
 */
open class OreTabPaneUI: LayerUI<JTabbedPane>() {
    companion object {
        // region Close button images; UI manager keys
        /**
         * Default "viewBox" used in [createCloseButtonImage] (default viewBox in [Material Design Icons](https://materialdesignicons.com/))
         */
        private val CLOSE_BUTTON_IMAGE__VIEW_BOX = Rectangle(2, 2, 20, 20)

        /**
         * Image for close button - simple diagonal cross
         *
         * "Programmatic" copy of [icon "close" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close.svg)
         *
         * Used in [createCloseButtonImage]
         */
        private val CLOSE_BUTTON_IMAGE__PATH__CROSS = Path2D.Double().apply {
            this.moveTo(19.0, 6.41) // M19,6.41
            this.lineTo(17.59, 5.0) // L17.59,5
            this.lineTo(12.0, 10.59) // L12,10.59
            this.lineTo(6.41, 5.0) // L6.41,5
            this.lineTo(5.0, 6.41) // L5,6.41
            this.lineTo(10.59, 12.0) // L10.59,12
            this.lineTo(5.0, 17.59) // L5,17.59
            this.lineTo(6.41, 19.0) // L6.41,19
            this.lineTo(12.0, 13.41) // L12,13.41
            this.lineTo(17.59, 19.0) // L17.59,19
            this.lineTo(19.0, 17.59) // L19,17.59
            this.lineTo(13.41, 12.0) // L13.41,12
            this.lineTo(19.0, 6.41) // L19,6.41
            this.closePath() // Z
        }

        /**
         * Image for close button - diagonal cross inside circle
         *
         * "Programmatic" copy of [icon "close-circle" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close-circle.svg)
         *
         * Used in [createCloseButtonImage]
         */
        private val CLOSE_BUTTON_IMAGE__PATH__CIRCLE: Path2D.Double = Path2D.Double().apply {
            this.moveTo(12.0, 2.0) // M12,2
            this.curveTo(17.53, 2.0, 22.0, 6.47, 22.0, 12.0) // C17.53,2 22,6.47 22,12
            this.curveTo(22.0, 17.53, 17.53, 22.0, 12.0, 22.0) // C22,17.53 17.53,22 12,22
            this.curveTo(6.47, 22.0, 2.0, 17.53, 2.0, 12.0) // C6.47,22 2,17.53 2,12
            this.curveTo(2.0, 6.47, 6.47, 2.0, 12.0, 2.0) // C2,6.47 6.47,2 12,2
            this.moveTo(15.59, 7.0) // M15.59,7
            this.lineTo(12.0, 10.59) // L12,10.59
            this.lineTo(8.41, 7.0) // L8.41,7
            this.lineTo(7.0 ,8.41) // L7,8.41
            this.lineTo(10.59, 12.0) // L10.59,12
            this.lineTo(7.0, 15.59) // L7,15.59
            this.lineTo(8.41, 17.0) // L8.41,17
            this.lineTo(12.0, 13.41) // L12,13.41
            this.lineTo(15.59, 17.0) // L15.59,17
            this.lineTo(17.0, 15.59) // L17,15.59
            this.lineTo(13.41, 12.0) // L13.41,12
            this.lineTo(17.0, 8.41) // L17,8.41
            this.lineTo(15.59, 7.0) // L15.59,7
            this.closePath() // Z
        }

        /**
         * Create an image for the close button
         *
         * If `circle` is `true`, then uses copy of [icon "close-circle" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close-circle.svg);
         * otherwise - uses copy of [icon "close" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close.svg)
         *
         * Used icon will be resized to size `width` × `height`
         *
         * @param color Color
         * @param circle If set to `true`, a cross will be drawn within a circle; otherwise - just a cross will be drawn
         * @param width Image width in pixels; default - [CLOSE_BUTTON__WIDTH]
         * @param height Image height in pixels; default - [CLOSE_BUTTON__HEIGHT]
         *
         * @return Image for close button
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun createCloseButtonImage(color: Color, circle: Boolean, width: Int = CLOSE_BUTTON__WIDTH, height: Int = CLOSE_BUTTON__HEIGHT): BufferedImage {
            val originalPath = if (circle) { CLOSE_BUTTON_IMAGE__PATH__CIRCLE } else { CLOSE_BUTTON_IMAGE__PATH__CROSS }

            val info = OreSwingShapeInfo(Rectangle(CLOSE_BUTTON_IMAGE__VIEW_BOX), Path2D.Double(originalPath))
            info.scale(width / info.viewBox.width, height / info.viewBox.height)

            return info.toImage(OreGraphicsConfig(color = color))
        }


        /**
         * Default value for [closeButtonImageIdle]
         *
         * Image for close button in the normal state (no hover and no click)
         *
         * Simple diagonal cross
         *
         * Copy of [icon "close" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close.svg)
         * painted with `java.awt.Color(0, 0, 0, 100)`
         */
        val CLOSE_BUTTON_IMAGE__IDLE: BufferedImage = createCloseButtonImage(Color(0, 0, 0, 100), false)

        /**
         * Key of default value for [closeButtonImageIdle]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON_IMAGE__IDLE: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON_IMAGE__IDLE"

        /**
         * Default value for [closeButtonImageHovered]
         *
         * Image for close button on hover but without pressing
         *
         * Diagonal cross inside circle
         *
         * Copy of [icon "close-circle" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close-circle.svg)
         * painted with `java.awt.Color(0, 0, 0, 100)`
         */
        val CLOSE_BUTTON_IMAGE__HOVERED: BufferedImage = createCloseButtonImage(Color(0, 0, 0, 100), true)

        /**
         * Key of default value for [closeButtonImageHovered]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON_IMAGE__HOVERED"

        /**
         * Default value for [closeButtonImagePressed]
         *
         * Image for close button on hover and pressing
         *
         * Diagonal cross inside circle
         *
         * Copy of [icon "close-circle" in Material Design Icons](https://github.com/Templarian/MaterialDesign/blob/master/svg/close-circle.svg)
         * painted with `java.awt.Color(0, 0, 0, 200)`
         */
        val CLOSE_BUTTON_IMAGE__PRESSED: BufferedImage = createCloseButtonImage(Color(0, 0, 0, 200), true)

        /**
         * Key of default value for [closeButtonImagePressed]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON_IMAGE__PRESSED"
        // endregion


        // region Close button sizes and coordinates; UI manager keys
        /**
         * Default close button width, in pixels (`12` pixels)
         *
         * Default value for [closeButtonSize].[width][Dimension.width]
         */
        const val CLOSE_BUTTON__WIDTH: Int = 12

        /**
         * Key of default value for [closeButtonSize].[width][Dimension.width]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__WIDTH: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__WIDTH"

        /**
         * Default close button height, in pixels (`12` pixels)
         *
         * Default value for [closeButtonSize].[height][Dimension.height]
         */
        const val CLOSE_BUTTON__HEIGHT: Int = 12

        /**
         * Key of default value for [closeButtonSize].[height][Dimension.height]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__HEIGHT: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__HEIGHT"


        /**
         * The default padding of the close button from the top edge of the close button area, in pixels (`2` pixels)
         *
         * Used when calculating [closeButtonPlaceSize]
         */
        const val CLOSE_BUTTON__PADDING_TOP: Int = 2

        /**
         * Key for default padding of the close button from the top edge of the close button area
         * (see description for [closeButtonPlaceSize])
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__PADDING_TOP: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__PADDING_TOP"

        /**
         * The default padding of the close button from the left edge of the close button area, in pixels (`2` pixels)
         *
         * Used when calculating [closeButtonPlaceSize]
         */
        const val CLOSE_BUTTON__PADDING_LEFT: Int = 2

        /**
         * Key for default padding of the close button from the left edge of the close button area
         * (see description for [closeButtonPlaceSize])
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__PADDING_LEFT: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__PADDING_LEFT"

        /**
         * The default padding of the close button from the bottom edge of the close button area, in pixels (`2` pixels)
         *
         * Used when calculating [closeButtonPlaceSize]
         */
        const val CLOSE_BUTTON__PADDING_BOTTOM: Int = 2

        /**
         * Key for default padding of the close button from the bottom edge of the close button area
         * (see description for [closeButtonPlaceSize])
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__PADDING_BOTTOM"

        /**
         * The default padding of the close button from the right edge of the close button area, in pixels (`2` pixels)
         *
         * Used when calculating [closeButtonPlaceSize]
         */
        const val CLOSE_BUTTON__PADDING_RIGHT: Int = 2

        /**
         * Key for default padding of the close button from the right edge of the close button area
         * (see description for [closeButtonPlaceSize])
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__PADDING_RIGHT: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__PADDING_RIGHT"


        /**
         * The default offset of the close button relative to the close button's area, on x-axis, in pixels (`-1` pixel)
         *
         * Default value for [closeButtonTranslate].[x][Point.x]
         */
        const val CLOSE_BUTTON__TRANSLATE_X: Int = -1

        /**
         * Key of default value for [closeButtonTranslate].[x][Point.x]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__TRANSLATE_X: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__TRANSLATE_X"

        /**
         * The default offset of the close button relative to the close button's area, on y-axis, in pixels (`0` pixels)
         *
         * Default value for [closeButtonTranslate].[y][Point.y]
         */
        const val CLOSE_BUTTON__TRANSLATE_Y: Int = 0

        /**
         * Key of default value for [closeButtonTranslate].[y][Point.y]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__CLOSE_BUTTON__TRANSLATE_Y: String = "me.ore.swing.ext.tabs.OreTabPaneUI.CLOSE_BUTTON__TRANSLATE_Y"
        // endregion


        // region Drop location color
        /**
         * Default color for drawing drop location (`java.awt.Color(0, 97, 168, 127)`)
         *
         * Default value for [dropLocationColor]
         */
        val DROP_LOCATION__COLOR: Color = Color(0, 97, 168, 127)

        /**
         * Key of default value for [dropLocationColor]
         *
         * Used to get a value using [UIManager.get]
         */
        const val UI_KEY__DROP_LOCATION__COLOR: String = "me.ore.swing.ext.tabs.OreTabPaneUI.DROP_LOCATION__COLOR"
        // endregion


        /**
         * Checks and optionally resizes `image`
         *
         * @param image Original image
         * @param requiredSize Required image size
         *
         * @return If the size of `image` equals `requiredSize`, return `image`; otherwise, returns a scaled copy of `image` with size equal to `requiredSize`
         */
        private fun checkImageSize(image: Image, requiredSize: Dimension): Image {
            val requiredWidth = requiredSize.width
            val requiredHeight = requiredSize.height

            return if ((image.getWidth(null) == requiredWidth) && (image.getHeight(null) == requiredHeight)) {
                image
            } else {
                image.getScaledInstance(requiredWidth, requiredHeight, Image.SCALE_SMOOTH)
            }
        }
    }


    // region UI defaults
    /**
     * Getting the value of the `requiredClass` class using [UIManager.get]
     *
     * @param T Result type
     * @param requiredClass Required result class
     * @param key Default value key, used to look up the value using `UIManager.get()`
     * @param defaultValue The value to return if `UIManager.get()` does not return a valid value
     *
     * @return If `UIManager.get()` returns `null` or a class object that is not assignable to `requiredClass` then this method returns `defaultValue`;
     * otherwise, the value obtained by calling `UIManager.get()`
     */
    @Suppress("UNCHECKED_CAST")
    protected open fun <T> getUiDefaults(requiredClass: Class<T>, key: String, defaultValue: T): T {
        val loaded = try {
            val value = UIManager.get(key)
            when {
                (value == null) -> null
                (requiredClass.isAssignableFrom(value.javaClass)) -> value as T
                else -> error("Invalid value for key \"$key\" in UI manager - expected ${requiredClass.name}, but found ${value.javaClass.name}")
            }
        } catch (e: Exception) {
            OreSwingExt.handle(e)
            null
        }

        return loaded ?: defaultValue
    }

    /**
     * Getting [Int] value using [UIManager.get]
     *
     * @param key Default value key, used to look up the value using `UIManager.get()`
     * @param defaultValue The value to return if `UIManager.get()` does not return a valid value
     *
     * @return If `UIManager.get()` returns `null` or a class object that is not assignable to `Int` class then this method returns `defaultValue`;
     * otherwise, the value obtained by calling `UIManager.get()`
     */
    protected open fun getUiDefaultsInt(key: String, defaultValue: Int): Int = this.getUiDefaults(Int::class.java, key, defaultValue)

    /**
     * Getting [Image] value using [UIManager.get]
     *
     * @param key Default value key, used to look up the value using `UIManager.get()`
     * @param defaultValue The value to return if `UIManager.get()` does not return a valid value
     * @param requiredSize If not equal to `null`, the resulting image will be scaled to this size
     *
     * @return If `UIManager.get()` returns `null` or a class object that is not assignable to `Image` class then this method returns `defaultValue`;
     * otherwise, the value obtained by calling `UIManager.get()`
     */
    protected open fun getUiDefaultsImage(key: String, defaultValue: Image, requiredSize: Dimension? = null): Image {
        return if (requiredSize == null) {
            this.getUiDefaults(Image::class.java, key, defaultValue)
        } else {
            checkImageSize(this.getUiDefaults(Image::class.java, key, defaultValue), requiredSize)
        }
    }


    /**
     * Creating the size of the close button
     *
     * Result will have values:
     *
     * * [width][Dimension.width] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__WIDTH]), or [CLOSE_BUTTON__WIDTH]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__WIDTH)` does not return `Int`
     * * [height][Dimension.height] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__HEIGHT]), or [CLOSE_BUTTON__HEIGHT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__HEIGHT)` does not return `Int`
     *
     * @return Close button size
     */
    protected open fun createCloseButtonSize(): Dimension {
        return Dimension(
            this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__WIDTH, CLOSE_BUTTON__WIDTH),
            this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__HEIGHT, CLOSE_BUTTON__HEIGHT)
        )
    }

    /**
     * Creating the size of the close button area
     *
     * The values will be retrieved first:
     *
     * * "left padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_LEFT]), or [CLOSE_BUTTON__PADDING_LEFT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_LEFT)` does not return `Int`
     * * "right padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_RIGHT]), or [CLOSE_BUTTON__PADDING_RIGHT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_RIGHT)` does not return `Int`
     * * "top padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_TOP]), or [CLOSE_BUTTON__PADDING_TOP]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_TOP)` does not return `Int`
     * * "bottom padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM]), or [CLOSE_BUTTON__PADDING_BOTTOM]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM)` does not return `Int`
     *
     * Result will be Dimension(
     *
     * `closeButtonSize`.[width][Dimension.width] + "left padding" + "right padding",
     *
     * `closeButtonSize`.[height][Dimension.height] + "top padding" + "bottom padding"
     *
     * )
     *
     * @return Close button area size
     */
    @Suppress("GrazieInspection")
    protected open fun createCloseButtonPlaceSize(closeButtonSize: Dimension): Dimension {
        val paddingLeft = this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__PADDING_LEFT, CLOSE_BUTTON__PADDING_LEFT)
        val paddingRight = this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__PADDING_RIGHT, CLOSE_BUTTON__PADDING_RIGHT)
        val paddingTop = this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__PADDING_TOP, CLOSE_BUTTON__PADDING_TOP)
        val paddingBottom = this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM, CLOSE_BUTTON__PADDING_BOTTOM)

        return Dimension(
            closeButtonSize.width + paddingLeft + paddingRight,
            closeButtonSize.height + paddingTop + paddingBottom

        )
    }

    /**
     * Creating a close button offset relative to the close button area
     *
     * Result will have:
     *
     * * [x][Point.x] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__TRANSLATE_X]), or [CLOSE_BUTTON__TRANSLATE_X]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__TRANSLATE_X)` does not return `Int`
     * * [y][Point.y] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__TRANSLATE_Y]), or [CLOSE_BUTTON__TRANSLATE_Y]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__TRANSLATE_Y)` does not return `Int`
     *
     * @return Offset of the close button relative to the close button area
     */
    protected open fun createCloseButtonTranslate(): Point {
        return Point(
            this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__TRANSLATE_X, CLOSE_BUTTON__TRANSLATE_X),
            this.getUiDefaultsInt(UI_KEY__CLOSE_BUTTON__TRANSLATE_Y, CLOSE_BUTTON__TRANSLATE_Y)
        )
    }


    /**
     * Creating a close button image in the normal state (no hover and no click)
     *
     * By default, this method returns [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__IDLE]), or [CLOSE_BUTTON_IMAGE__IDLE]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__IDLE)` does not return `Image`. Result image will be scale to `closeButtonSize`
     *
     * @param closeButtonSize Close button size
     *
     * @return Image of the close button in normal state
     */
    protected open fun createCloseButtonImageIdle(closeButtonSize: Dimension): Image =
        this.getUiDefaultsImage(UI_KEY__CLOSE_BUTTON_IMAGE__IDLE, CLOSE_BUTTON_IMAGE__IDLE, closeButtonSize)

    /**
     * Creating a close button image on hover but without pressing
     *
     * By default, this method returns [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED]), or [CLOSE_BUTTON_IMAGE__HOVERED]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED)` does not return `Image`. Result image will be scale to `closeButtonSize`
     *
     * @param closeButtonSize Close button size
     *
     * @return Close button image on hover but without pressing
     */
    protected open fun createCloseButtonImageHovered(closeButtonSize: Dimension): Image =
        this.getUiDefaultsImage(UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED, CLOSE_BUTTON_IMAGE__HOVERED, closeButtonSize)

    /**
     * Create close button image on hover and pressing
     *
     * By default, this method returns [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED]), or [CLOSE_BUTTON_IMAGE__PRESSED]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED)` does not return `Image`. Result image will be scale to `closeButtonSize`
     *
     * @param closeButtonSize Close button size
     *
     * @return Close button image on hover and pressing
     */
    protected open fun createCloseButtonImagePressed(closeButtonSize: Dimension): Image =
        this.getUiDefaultsImage(UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED, CLOSE_BUTTON_IMAGE__PRESSED, closeButtonSize)


    /**
     * Creating a drop location drawing color
     *
     * By default - [Color] stored in [UIManager] with key [UI_KEY__DROP_LOCATION__COLOR] (if exists) or [DROP_LOCATION__COLOR]
     *
     * @return Drop location drawing color
     */
    protected open fun createDropLocationColor(): Color = this.getUiDefaults(Color::class.java, UI_KEY__DROP_LOCATION__COLOR, DROP_LOCATION__COLOR)



    /**
     * Updates properties:
     *
     * * [_closeButtonSize] = [createCloseButtonSize] ()
     * * [_closeButtonPlaceSize] = [createCloseButtonPlaceSize] (`_closeButtonSize`)
     * * [_closeButtonTranslate] = [createCloseButtonTranslate] ()
     * * [_closeButtonImageIdle] = [createCloseButtonImageIdle] (`_closeButtonSize`)
     * * [_closeButtonImageHovered] = [createCloseButtonImageHovered] (`_closeButtonSize`)
     * * [_closeButtonImagePressed] = [createCloseButtonImagePressed] (`_closeButtonSize`)
     * * [_dropLocationColor] = [createDropLocationColor] ()
     *
     * After that, it runs the [uiConfigChanged][OreTabPaneUIListener.uiConfigChanged] method for all listeners
     */
    protected open fun updateUiDefaults() {
        val closeButtonSize = this.createCloseButtonSize()
        this._closeButtonSize = closeButtonSize
        this._closeButtonPlaceSize = this.createCloseButtonPlaceSize(closeButtonSize)
        this._closeButtonTranslate = this.createCloseButtonTranslate()
        this._closeButtonImageIdle = this.createCloseButtonImageIdle(closeButtonSize)
        this._closeButtonImageHovered = this.createCloseButtonImageHovered(closeButtonSize)
        this._closeButtonImagePressed = this.createCloseButtonImagePressed(closeButtonSize)
        this._dropLocationColor = this.createDropLocationColor()

        this.forEachListener { it.uiConfigChanged(this) }
    }
    // endregion


    // region Sizes & coordinates
    /**
     * Close button size; mutable version of [closeButtonSize]
     *
     * By default - the result of calling [createCloseButtonSize]
     */
    @Suppress("PropertyName")
    protected var _closeButtonSize: Dimension = this.createCloseButtonSize()

    /**
     * Close button size
     *
     * By default, it has values:
     *
     * * [width][Dimension.width] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__WIDTH]), or [CLOSE_BUTTON__WIDTH]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__WIDTH)` does not return `Int`
     * * [height][Dimension.height] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__HEIGHT]), or [CLOSE_BUTTON__HEIGHT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__HEIGHT)` does not return `Int`
     */
    open val closeButtonSize: Dimension
        get() = this._closeButtonSize


    /**
     * The size of the close button area; mutable version of [closeButtonPlaceSize]
     *
     * By default - the result of calling [createCloseButtonPlaceSize] ([_closeButtonSize])
     */
    @Suppress("PropertyName")
    protected var _closeButtonPlaceSize: Dimension = this.createCloseButtonPlaceSize(this._closeButtonSize)

    /**
     * Size of close button area (must be equal to or greater than [closeButtonSize])
     *
     * By default, it uses "virtual" values:
     *
     * * "left padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_LEFT]), or [CLOSE_BUTTON__PADDING_LEFT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_LEFT)` does not return `Int`
     * * "right padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_RIGHT]), or [CLOSE_BUTTON__PADDING_RIGHT]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_RIGHT)` does not return `Int`
     * * "top padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_TOP]), or [CLOSE_BUTTON__PADDING_TOP]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_TOP)` does not return `Int`
     * * "bottom padding" - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM]), or [CLOSE_BUTTON__PADDING_BOTTOM]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__PADDING_BOTTOM)` does not return `Int`
     *
     * which are used when creating the result:
     *
     * Dimension(
     *
     * `closeButtonSize`.[width][Dimension.width] + "left padding" + "right padding",
     *
     * `closeButtonSize`.[height][Dimension.height] + "top padding" + "bottom padding"
     *
     * )
     */
    @Suppress("GrazieInspection")
    open val closeButtonPlaceSize: Dimension
        get() = this._closeButtonPlaceSize


    /**
     * Offset of the close button relative to the close button area; mutable version of [closeButtonTranslate]
     *
     * By default - the result of calling [createCloseButtonTranslate]
     */
    @Suppress("PropertyName")
    protected var _closeButtonTranslate: Point = this.createCloseButtonTranslate()

    /**
     * Offset of the close button relative to the close button area
     *
     * By default, it has values:
     *
     * * [x][Point.x] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__TRANSLATE_X]), or [CLOSE_BUTTON__TRANSLATE_X]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__TRANSLATE_X)` does not return `Int`
     * * [y][Point.y] - [Int] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON__TRANSLATE_Y]), or [CLOSE_BUTTON__TRANSLATE_Y]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON__TRANSLATE_Y)` does not return `Int`
     */
    open val closeButtonTranslate: Point
        get() = this._closeButtonTranslate
    // endregion


    // region Close button images
    /**
     * The image of the close button in the normal state (without hovering and pressing); mutable version of [closeButtonImageIdle]
     *
     * By default - result of calling [createCloseButtonImageIdle] ([_closeButtonSize])
     */
    @Suppress("PropertyName")
    protected var _closeButtonImageIdle: Image = this.createCloseButtonImageIdle(this._closeButtonSize)

    /**
     * The image of the close button in the normal state (without hovering and pressing)
     *
     * By default - [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__IDLE]), or [CLOSE_BUTTON_IMAGE__IDLE]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__IDLE)` does not return `Image`. Image is scaled to [closeButtonSize]
     */
    open val closeButtonImageIdle: Image
        get() = this._closeButtonImageIdle


    /**
     * An image of a close button on hover, but without pressing; mutable version of closeButtonImageHovered
     *
     * By default - result of calling [createCloseButtonImageHovered] ([_closeButtonSize])
     */
    @Suppress("PropertyName")
    protected var _closeButtonImageHovered: Image = this.createCloseButtonImageHovered(this._closeButtonSize)

    /**
     * An image of a close button on hover, but without pressing
     *
     * By default - [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED]), or [CLOSE_BUTTON_IMAGE__HOVERED]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__HOVERED)` does not return `Image`. Image is scaled to [closeButtonSize]
     */
    open val closeButtonImageHovered: Image
        get() = this._closeButtonImageHovered


    /**
     * Close button image on hover and pressing; mutable version of [closeButtonImagePressed]
     *
     * By default - result of calling [createCloseButtonImagePressed] ([_closeButtonSize])
     */
    @Suppress("PropertyName")
    protected var _closeButtonImagePressed: Image = this.createCloseButtonImagePressed(this._closeButtonSize)

    /**
     * Close button image on hover and pressing
     *
     * By default - [Image] found with [UIManager.get] ([UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED]), or [CLOSE_BUTTON_IMAGE__PRESSED]
     * if `UIManager.get(UI_KEY__CLOSE_BUTTON_IMAGE__PRESSED)` does not return `Image`. Image is scaled to [closeButtonSize]
     */
    open val closeButtonImagePressed: Image
        get() = this._closeButtonImagePressed
    // endregion


    // region Listeners
    /**
     * TODO translate
     */
    @Suppress("FunctionName")
    protected open fun _createListeners(): MutableSet<OreTabPaneUIListener> = HashSet()

    /**
     * A set of listeners; mutable version of [listeners]
     */
    @Suppress("PropertyName")
    protected val _listeners: MutableSet<OreTabPaneUIListener> = this._createListeners()


    /**
     * TODO translate
     */
    protected open fun createListeners(mutable: MutableSet<OreTabPaneUIListener>): Set<OreTabPaneUIListener> = Collections.unmodifiableSet(mutable)

    /**
     * A set of listeners
     */
    @Suppress("unused")
    open val listeners: Set<OreTabPaneUIListener> = this.createListeners(this._listeners)


    /**
     * Executes a [block] for each of the [listeners]
     *
     * @param block Executable block
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun forEachListener(block: (listener: OreTabPaneUIListener) -> Unit) {
        ArrayList(this._listeners).forEach { listener ->
            try {
                block(listener)
            } catch (e: Exception) {
                OreSwingExt.handle(e)
            }
        }
    }

    /**
     * Adding a listener
     *
     * @param listener Added listener
     *
     * @return `true` if the listener was not in [listeners] and was added as a result of the current call; otherwise - `false`
     */
    open fun addListener(listener: OreTabPaneUIListener): Boolean = this._listeners.add(listener)

    /**
     * Removing a listener
     *
     * @param listener Removed listener
     *
     * @return `true` if the listener was in [listeners] and was removed as a result of the current call; otherwise - `false`
     */
    open fun removeListener(listener: OreTabPaneUIListener): Boolean = this._listeners.remove(listener)
    // endregion


    // region Utils
    /**
     * If [component] is an object of class [JLayer] and [view][JLayer.view] in it is an object of class [JTabbedPane],
     * then runs [block] on the found `JLayer` and `view`
     *
     * @param component Component
     * @param block Executable block
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun processLayerAndPane(component: JComponent?, block: (layer: JLayer<*>, pane: JTabbedPane) -> Unit) {
        val layer = (component as? JLayer<*>)
            ?: return
        val pane = (layer.view as? JTabbedPane)
            ?: return
        block(layer, pane)
    }

    /**
     * Calls [block] for each [OreTabTitle] in the given [pane]
     * (each [OreTabTitle] is taken as [pane].[getTabComponentAt][JTabbedPane.getTabComponentAt] (index))
     *
     * @param pane Tab pane
     * @param block Executable block
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected inline fun forEachTabTitle(pane: JTabbedPane, block: (index: Int, tabTitle: OreTabTitle) -> Unit) {
        for (index in 0 until pane.tabCount) {
            val tabTitle = (pane.getTabComponentAt(index) as? OreTabTitle)
                ?: continue
            block(index, tabTitle)
        }
    }

    /**
     * Getting the bounds of the close button for the tab in [pane] with the specified [index]
     *
     * Returns `null` in the following cases:
     * * tab not showing
     * * tab title component is not an instance of [OreTabTitle] or its [closeable][OreTabTitle.closeable] property is `false`
     *
     * @param pane Tab pane
     * @param index Index of tab
     *
     * @return Close button bounds, or `null` if no bounds can be defined
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun getCloseButtonBounds(pane: JTabbedPane, index: Int): Rectangle? {
        return run calcBounds@ {
            val tabTitle = (pane.getTabComponentAt(index) as? OreTabTitle)
                ?: return@calcBounds null
            if (!tabTitle.closeable)
                return@calcBounds null

            val tabBounds = pane.getBoundsAt(index)
                ?: return@calcBounds null

            val placeSize = this.closeButtonPlaceSize
            val size = this.closeButtonSize
            val translate = this.closeButtonTranslate

            val tabTitleBounds = tabTitle.bounds
            var current: Component? = tabTitle.parent
            while ((current != null) && (current != pane)) {
                current.bounds.let { tabTitleBounds.translate(it.x, it.y) }
                current = current.parent
            }

            val x = tabBounds.x + tabBounds.width - placeSize.width  + translate.x
            val y = tabTitleBounds.y + ((tabTitleBounds.height - size.height) / 2) + translate.y
            val width = size.width
            val height = size.height

            Rectangle(x, y, width, height)
        }
    }
    // endregion


    // region Install/uninstall UI
    override fun installUI(c: JComponent?) {
        super.installUI(c)
        (c as? JLayer<*>)?.let {
            it.layerEventMask = AWTEvent.MOUSE_EVENT_MASK or AWTEvent.MOUSE_MOTION_EVENT_MASK
        }
    }

    override fun updateUI(l: JLayer<out JTabbedPane>?) {
        super.updateUI(l)
        this.updateUiDefaults()
    }

    override fun uninstallUI(c: JComponent?) {
        super.uninstallUI(c)
        (c as? JLayer<*>)?.let {
            it.layerEventMask = 0
        }
    }
    // endregion


    override fun paint(g: Graphics?, c: JComponent?) {
        super.paint(g, c)

        val graphics = (g as? Graphics2D)
            ?: return

        this.processLayerAndPane(c) { _, pane ->
            this.forEachTabTitle(pane) { index, tabTitle ->
                if (tabTitle.closeable) {
                    val closeButtonBounds = this.getCloseButtonBounds(pane, index)
                        ?: return@forEachTabTitle

                    val sourceImage = this.tabMouseInfo?.let { tabMouseInfo ->
                        if (tabMouseInfo.index == index) {
                            when (tabMouseInfo.closeButtonState) {
                                CloseButtonState.IDLE -> this.closeButtonImageIdle
                                CloseButtonState.HOVERED -> this.closeButtonImageHovered
                                CloseButtonState.PRESSED -> this.closeButtonImagePressed
                            }
                        } else {
                            null
                        }
                    } ?: this.closeButtonImageIdle

                    val image = sourceImage.let { image ->
                        val imageWidth = image.getWidth(null)
                        val imageHeight = image.getHeight(null)
                        if ((imageWidth != closeButtonBounds.width) || (imageHeight != closeButtonBounds.height)) {
                            image.getScaledInstance(closeButtonBounds.width, closeButtonBounds.height, Image.SCALE_SMOOTH)!!
                        } else {
                            image
                        }
                    }

                    graphics.drawImage(image, closeButtonBounds.x, closeButtonBounds.y, null)
                }
            }

            this.dropInfo?.let paintDropLocation@ { (draggedIndex, targetIndex) ->
                graphics.color = this.dropLocationColor

                val actualTargetIndex: Int?
                val paintBefore: Boolean
                if (draggedIndex == null) {
                    if (targetIndex == null) {
                        paintBefore = false
                        actualTargetIndex = pane.tabCount.let {
                            if (it == 0) {
                                null
                            } else {
                                it - 1
                            }
                        }
                    } else {
                        paintBefore = true
                        actualTargetIndex = targetIndex
                    }
                } else {
                    actualTargetIndex = targetIndex
                        ?: pane.tabCount.let {
                            if (it == 0) {
                                null
                            } else {
                                it - 1
                            }
                        }

                    paintBefore = (actualTargetIndex != null) && (draggedIndex > actualTargetIndex)
                }

                if (actualTargetIndex == null) {
                    val width = pane.width
                    val height = pane.height
                    graphics.fillRect(0, 0, width, height)
                } else {
                    val targetTabBounds: Rectangle? = pane.getBoundsAt(actualTargetIndex)

                    if (targetTabBounds == null) {
                        val width = pane.width
                        val height = pane.height
                        graphics.fillRect(0, 0, width, height)
                    } else {
                        val vertical = pane.tabPlacement.let { tabPlacement ->
                            (tabPlacement == JTabbedPane.LEFT) || (tabPlacement == JTabbedPane.RIGHT)
                        }

                        val dropLocationBounds = if (vertical) {
                            val size = Dimension(targetTabBounds.width, 8)

                            val location = if (paintBefore) {
                                Point(targetTabBounds.x, targetTabBounds.y - 4)
                            } else {
                                Point(targetTabBounds.x, targetTabBounds.y + targetTabBounds.height - 4)
                            }

                            Rectangle(location, size)
                        } else {
                            val size = Dimension(8, targetTabBounds.height)

                            val location = if (paintBefore) {
                                Point(targetTabBounds.x - 4, targetTabBounds.y)
                            } else {
                                Point(targetTabBounds.x + targetTabBounds.width - 4, targetTabBounds.y)
                            }

                            Rectangle(location, size)
                        }

                        graphics.fillRect(
                            dropLocationBounds.x,
                            dropLocationBounds.y,
                            dropLocationBounds.width,
                            dropLocationBounds.height
                        )
                    }
                }
            }
        }
    }


    // region Mouse events, tab mouse state
    /**
     * Close button state
     */
    protected enum class CloseButtonState {
        /**
         * Normal state
         */
        IDLE,

        /**
         * Cursor hovered over close button
         */
        HOVERED,

        /**
         * Close button pressed
         */
        PRESSED
    }

    /**
     * Tab information related to mouse state
     *
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param closeButtonState Close button state
     */
    protected data class TabMouseInfo(
        val layer: JLayer<out JTabbedPane>,
        val pane: JTabbedPane,
        val index: Int,
        val closeButtonState: CloseButtonState
    )


    /**
     * Tab information related to mouse state
     *
     * Triggers a redrawing of [pane][TabMouseInfo.pane] when changed
     */
    protected open var tabMouseInfo: TabMouseInfo? = null
        set(new) {
            if (field != new) {
                val old = field
                field = new

                new?.let {
                    val tabBounds = new.pane.getBoundsAt(new.index)
                    if (tabBounds == null) {
                        new.pane.repaint()
                    } else {
                        new.pane.repaint(tabBounds)
                    }
                }

                if ((new == null) && (old != null)) {
                    val tabBounds = if (old.index < old.pane.tabCount) {
                        old.pane.getBoundsAt(old.index)
                    } else {
                        null
                    }

                    if (tabBounds == null) {
                        old.pane.repaint()
                    } else {
                        old.pane.repaint(tabBounds)
                    }
                }
            }
        }


    override fun processMouseEvent(event: MouseEvent?, layer: JLayer<out JTabbedPane>?) {
        this.processLayerMouseEvent(event, layer)
    }

    override fun processMouseMotionEvent(event: MouseEvent?, layer: JLayer<out JTabbedPane>?) {
        this.processLayerMouseEvent(event, layer)
    }

    /**
     * Handling mouse events
     *
     * Called from [processMouseEvent] and [processMouseMotionEvent]
     *
     * Aborts execution if:
     *
     * * [layer] is `null`
     * * [layer].[view][JLayer.view] is not [JTabbedPane]
     * * [event] is `null`
     * * [event].[point][MouseEvent.getPoint] is `null`
     * * in the resulting [JTabbedPane] no tabs match the coordinates of [event].[point][MouseEvent.getPoint]
     *
     * Depending on [event].[id][MouseEvent.id] calls one of the methods:
     * [tabClicked], [tabMousePressed], [tabMouseReleased], [tabMouseMoved], [tabMouseEntered], [tabMouseExited], [tabMouseDragged]
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     */
    protected open fun processLayerMouseEvent(event: MouseEvent?, layer: JLayer<out JTabbedPane>?) {
        layer ?: run {
            this.tabMouseInfo = null
            return
        }
        val pane = layer.view ?: run {
            this.tabMouseInfo = null
            layer.repaint()
            return
        }

        event ?: run {
            this.tabMouseInfo = null
            pane.repaint()
            return
        }
        val point = event.point ?: run {
            this.tabMouseInfo = null
            pane.repaint()
            return
        }

        val index = pane.indexAtLocation(point.x, point.y)
        if (index < 0) {
            this.tabMouseInfo = null
            return
        }

        val closeButtonBounds = this.getCloseButtonBounds(pane, index)
        val onCloseButton = closeButtonBounds?.contains(point) ?: false
        when (event.id) {
            MouseEvent.MOUSE_CLICKED -> this.tabClicked(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_PRESSED -> this.tabMousePressed(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_RELEASED -> this.tabMouseReleased(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_MOVED -> this.tabMouseMoved(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_ENTERED -> this.tabMouseEntered(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_EXITED -> this.tabMouseExited(event, layer, pane, index, onCloseButton)
            MouseEvent.MOUSE_DRAGGED -> this.tabMouseDragged(event, layer, pane, index, onCloseButton)
        }
    }


    /**
     * Called from [processLayerMouseEvent] when a tab is clicked
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabClicked(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        if (onCloseButton) {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.HOVERED)
            this.forEachListener { it.tabCloseClicked(this, index) }
        } else {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.IDLE)
        }
    }

    /**
     * Called from [processLayerMouseEvent] when a mouse button is pressed on a tab
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMousePressed(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        if (onCloseButton) {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.PRESSED)
        } else {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.IDLE)
        }
    }

    /**
     * Called from [processLayerMouseEvent] when the mouse button is released on a tab
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMouseReleased(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        if (onCloseButton) {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.HOVERED)
        } else {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.IDLE)
        }
    }

    /**
     * Called from [processLayerMouseEvent] when the mouse cursor moves on a tab
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMouseMoved(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        if (onCloseButton) {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.HOVERED)
        } else {
            this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.IDLE)
        }
    }

    /**
     * Called from [processLayerMouseEvent] when the mouse cursor enters the tab pane and the cursor is on the tab
     *
     * Calls [tabMouseMoved] by default
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMouseEntered(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        this.tabMouseMoved(event, layer, pane, index, onCloseButton)
    }

    /**
     * Called from [processLayerMouseEvent] when the mouse cursor leaves the tab bar and the cursor was on a tab
     *
     * Clears [tabMouseInfo] by default
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMouseExited(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        this.tabMouseInfo = null
    }

    /**
     * Called from [processLayerMouseEvent] when the mouse cursor moves on a tab with the mouse button pressed
     *
     * @param event Mouse event
     * @param layer Tab pane decorator
     * @param pane Tab pane
     * @param index Index of tab
     * @param onCloseButton `true` means that the event happened on the close button
     */
    protected open fun tabMouseDragged(event: MouseEvent, layer: JLayer<out JTabbedPane>, pane: JTabbedPane, index: Int, onCloseButton: Boolean) {
        this.tabMouseInfo = TabMouseInfo(layer, pane, index, CloseButtonState.IDLE)
        this.forEachListener { it.tabDragged(this, event, index) }
    }
    // endregion


    // region Drop location
    /**
     * Drop location drawing color; mutable version of [dropLocationColor]
     *
     * By default - result of calling [createDropLocationColor]
     */
    @Suppress("PropertyName")
    protected var _dropLocationColor: Color = this.createDropLocationColor()

    /**
     * Drop location drawing color
     *
     *  By default - [Color] stored in [UIManager] with key [UI_KEY__DROP_LOCATION__COLOR] (if exists) or [DROP_LOCATION__COLOR]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val dropLocationColor: Color
        get() = this._dropLocationColor


    /**
     * Information for drawing of drop location
     *
     * @param draggedIndex The draggable tab index; filled if the dragged tab belongs to the current tab bar
     * @param targetIndex Index of tab under the mouse cursor; is `null` if there is no tab under the cursor
     */
    protected data class DropInfo(val draggedIndex: Int?, val targetIndex: Int?)


    /**
     * Information for drawing of drop location
     */
    protected open var dropInfo: DropInfo? = null

    /**
     * Called to remove the drop location
     *
     * @param pane Tab pane which drawn with drop location
     */
    fun clearDropLocation(pane: JTabbedPane) {
        this.dropInfo = null
        pane.repaint()
    }

    /**
     * Called to draw the drop location
     *
     * @param pane Tab pane on which to draw a drop location
     * @param draggedIndex The dragged tab index; filled if the dragged tab belongs to the current tab bar
     * @param targetIndex Index of tab under the mouse cursor; is `null` if there is no tab under the cursor
     */
    fun paintDropLocation(pane: JTabbedPane, draggedIndex: Int?, targetIndex: Int?) {
        this.dropInfo = DropInfo(draggedIndex = draggedIndex, targetIndex = targetIndex)
        pane.repaint()
    }
    // endregion
}
