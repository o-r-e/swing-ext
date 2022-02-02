package me.ore.swing.ext.tabs

import me.ore.swing.ext.OreSwingExtUtils
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JComponent
import javax.swing.JLayer
import javax.swing.JTabbedPane
import javax.swing.plaf.LayerUI
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


/**
 * Additional UI layer for [OreTabPane]
 */
@Suppress("LeakingThis")
open class OreTabPaneUI: LayerUI<JTabbedPane>() {
    companion object {
        // region Storing UIs
        /**
         * Map "UI -> null" with weak keys
         */
        private val UI_MAP = WeakHashMap<OreTabPaneUI, Any?>()

        /**
         * Stores an additional UI layer as key in [UI_MAP]
         *
         * This method is thread-safe
         *
         * @param ui Additional UI layer, which must be stored
         */
        private fun store(ui: OreTabPaneUI) {
            synchronized(UI_MAP) {
                UI_MAP[ui] = null
            }
        }

        /**
         * Getting all additional UI layers
         *
         * This method is thread-safe
         *
         * @return Additional UI layers
         */
        private fun getUiObjects(): Collection<OreTabPaneUI> {
            return synchronized(UI_MAP) {
                UI_MAP.keys.toList()
            }
        }
        // endregion


        // region Updating UI defaults
        /**
         * An indication that the default values are currently being updated
         */
        private var defaultsUpdating = false

        /**
         * Called to disable updating all [OreTabPane] when properties change:
         *
         * * [closeButtonDefaultColorIdle], [closeButtonDefaultColorHovered], [closeButtonDefaultColorPressed]
         * * [closeButtonWidth], [closeButtonHeight]
         * * [closeButtonPaddingTop], [closeButtonPaddingLeft], [closeButtonPaddingBottom], [closeButtonPaddingRight]
         * * [closeButtonTranslateX], [closeButtonTranslateY]
         * * [dropLocationDefaultColor]
         *
         * The [endUpdatingDefaults] method should always be called after this method
         *
         * This method is NOT thread-safe, it can only be run inside [EventQueue.dispatchThread]
         */
        fun startUpdatingDefaults() {
            defaultsUpdating = true
        }

        /**
         * If [defaultsUpdating] is `false`, then each object returned by the [getUiObjects] method will have the [updateUiDefaults] method called
         *
         * This method is NOT thread-safe, it can only be run inside [EventQueue.dispatchThread]
         */
        private fun updateDefaults() {
            if (defaultsUpdating)
                return

            getUiObjects().forEach { uiObject ->
                try {
                    uiObject.updateUiDefaults()
                } catch (e: Exception) {
                    OreSwingExtUtils.handle(e)
                }
            }
        }

        /**
         * Triggers an update of all [OreTabPane] to apply property values:
         *
         * * [closeButtonDefaultColorIdle], [closeButtonDefaultColorHovered], [closeButtonDefaultColorPressed]
         * * [closeButtonWidth], [closeButtonHeight]
         * * [closeButtonPaddingTop], [closeButtonPaddingLeft], [closeButtonPaddingBottom], [closeButtonPaddingRight]
         * * [closeButtonTranslateX], [closeButtonTranslateY]
         * * [dropLocationDefaultColor]
         *
         * This method should always run after calling [startUpdatingDefaults]
         *
         * This method is NOT thread-safe, it can only be run inside [EventQueue.dispatchThread]
         */
        fun endUpdatingDefaults() {
            defaultsUpdating = false
            updateDefaults()
        }

        /**
         * Order of execution:
         *
         * * [startUpdatingDefaults]
         * * [block]
         * * [endUpdatingDefaults] (in `finally` block)
         *
         * [block] is for changing one or more of the properties:
         *
         * * [closeButtonDefaultColorIdle], [closeButtonDefaultColorHovered], [closeButtonDefaultColorPressed]
         * * [closeButtonWidth], [closeButtonHeight]
         * * [closeButtonPaddingTop], [closeButtonPaddingLeft], [closeButtonPaddingBottom], [closeButtonPaddingRight]
         * * [closeButtonTranslateX], [closeButtonTranslateY]
         * * [dropLocationDefaultColor]
         *
         * This method is NOT thread-safe, it can only be run inside [EventQueue.dispatchThread]
         *
         * @param block Executable block
         */
        @Suppress("MemberVisibilityCanBePrivate", "unused")
        inline fun updateDefaults(block: () -> Unit) {
            try {
                startUpdatingDefaults()
                block()
            } finally {
                endUpdatingDefaults()
            }
        }
        // endregion


        // region Close button image & colors
        /**
         * Create an image for the close button
         *
         * @param color Color
         * @param circle If set to `true`, a cross will be drawn within a circle; otherwise - just a cross will be drawn
         * @param width Image width in pixels; default - `12`
         * @param height Image height in pixels; default - `12`
         * @param crossInsetsTop Indentation of the cross from the top edge of the image; defaults to ([height] / 4) when [circle] is `true` and ([height] / 6) when [circle] is `false`
         * @param crossInsetsLeft Indentation of the cross from the left edge of the image; default ([width] / 4) when [circle] is `true` and ([width] / 6) when [circle] is `false`
         * @param crossInsetsBottom Indentation of the cross from the bottom edge of the image; defaults to ([height] / 4) when [circle] is `true` and ([height] / 6) when [circle] is `false`
         * @param crossInsetsRight Indentation of the cross from the right edge of the image; default ([width] / 4) when [circle] is `true` and ([width] / 6) when [circle] is `false`
         * @param crossLineWidth The thickness of the lines of the cross in pixels; default - `1.5`
         *
         * @return Image for close button
         */
        @Suppress("MemberVisibilityCanBePrivate")
        fun createCloseButtonImage(
            color: Color,
            circle: Boolean,
            width: Int = 12,
            height: Int = 12,
            crossInsetsTop: Int = if (circle) (height / 4) else (height / 6),
            crossInsetsLeft: Int = if (circle) (width / 4) else (width / 6),
            crossInsetsBottom: Int = if (circle) (height / 4) else (height / 6),
            crossInsetsRight: Int = if (circle) (width / 4) else (width / 6),
            crossLineWidth: Float = 1.5f
        ): BufferedImage {
            val result = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

            val graphics = result.createGraphics()
                ?: error("Cannot create graphics for buffered image")

            try {
                graphics.setRenderingHints(mapOf(
                    RenderingHints.KEY_ANTIALIASING to RenderingHints.VALUE_ANTIALIAS_ON,
                    RenderingHints.KEY_FRACTIONALMETRICS to RenderingHints.VALUE_FRACTIONALMETRICS_ON
                ))
                graphics.color = color
                graphics.stroke = BasicStroke(crossLineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

                @Suppress("UnnecessaryVariable")
                val lineX1 = crossInsetsLeft
                val lineX2 = width - crossInsetsRight - 1
                @Suppress("UnnecessaryVariable")
                val lineY1 = crossInsetsTop
                val lineY2 = height - crossInsetsBottom - 1

                val composite = graphics.composite
                try {
                    if (circle) {
                        graphics.fillOval(0, 0, width, height)
                        graphics.composite = AlphaComposite.getInstance(AlphaComposite.CLEAR, 0f)
                    }
                    graphics.drawLine(lineX1, lineY1, lineX2, lineY2)
                    graphics.drawLine(lineX2, lineY1, lineX1, lineY2)
                } finally {
                    graphics.composite = composite
                }
            } finally {
                graphics.dispose()
            }

            return result
        }

        /**
         * Normal close button color
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonDefaultColorIdle: Color = Color(0, 0, 0, 100)
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * Close button color on hover
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonDefaultColorHovered: Color = Color(0, 0, 0, 100)
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * Close button color if pressed
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonDefaultColorPressed: Color = Color(0, 0, 0, 200)
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }
        // endregion


        // region Sizes & coordinates
        /**
         * Default close button width, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonWidth: Int = 12
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * Default close button height, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonHeight: Int = 12
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default padding of the close button from the top edge of the close button area, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonPaddingTop: Int = 2
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default padding of the close button from the left edge of the close button area, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonPaddingLeft: Int = 2
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default padding of the close button from the bottom edge of the close button area, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonPaddingBottom: Int = 2
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default padding of the close button from the right edge of the close button area, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonPaddingRight: Int = 2
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default offset of the close button relative to the close button's area, on x-axis, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonTranslateX: Int = -1
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }

        /**
         * The default offset of the close button relative to the close button's area, on y-axis, in pixels
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var closeButtonTranslateY: Int = 0
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }
        // endregion


        /**
         * Default color for drawing drop location
         *
         * On change, starts redrawing all [OreTabPane] if [startUpdatingDefaults] has not been called before or [endUpdatingDefaults] has already been called yet
         *
         * This property is NOT thread-safe, it can only be changed inside [EventQueue.dispatchThread]
         */
        var dropLocationDefaultColor: Color = Color(0, 97, 168, 127)
            set(new) {
                if (field != new) {
                    field = new
                    updateDefaults()
                }
            }
    }


    // region Sizes & coordinates
    /**
     * Creating the size of the close button
     *
     * By default - `Dimension`([closeButtonWidth], [closeButtonHeight])
     *
     * @return Close button size
     */
    protected open fun createCloseButtonSize(): Dimension = Dimension(closeButtonWidth, closeButtonHeight)

    /**
     * Close button size; mutable version of [closeButtonSize]
     *
     * By default - the result of calling [createCloseButtonSize]
     */
    @Suppress("PropertyName")
    protected open var _closeButtonSize: Dimension = this.createCloseButtonSize()

    /**
     * Close button size
     */
    open val closeButtonSize: Dimension
        get() = this._closeButtonSize


    /**
     * Creating the size of the close button area
     *
     * By default - Dimension(
     * closeButtonSize.width + [closeButtonPaddingLeft] + [closeButtonPaddingRight],
     * closeButtonSize.height + [closeButtonPaddingTop] + [closeButtonPaddingBottom]
     * )
     *
     * @return Close button area size
     */
    protected open fun createCloseButtonPlaceSize(closeButtonSize: Dimension): Dimension {
        return Dimension(
            closeButtonSize.width + closeButtonPaddingLeft + closeButtonPaddingRight,
            closeButtonSize.height + closeButtonPaddingTop + closeButtonPaddingBottom
        )
    }

    /**
     * The size of the close button area; mutable version of [closeButtonPlaceSize]
     *
     * By default - the result of calling [createCloseButtonPlaceSize] ([closeButtonSize])
     */
    @Suppress("PropertyName")
    protected open var _closeButtonPlaceSize: Dimension = this.createCloseButtonPlaceSize(this.closeButtonSize)

    /**
     * Size of close button area (must be equal to or greater than [closeButtonSize])
     */
    open val closeButtonPlaceSize: Dimension
        get() = this._closeButtonPlaceSize


    /**
     * Creating a close button offset relative to the close button area
     *
     * By default - Point([closeButtonTranslateX], [closeButtonTranslateY])
     *
     * @return Offset of the close button relative to the close button area
     */
    protected open fun createCloseButtonTranslate(): Point = Point(closeButtonTranslateX, closeButtonTranslateY)

    /**
     * Offset of the close button relative to the close button area; mutable version of [closeButtonTranslate]
     *
     * By default - the result of calling [createCloseButtonTranslate]
     */
    @Suppress("PropertyName")
    protected open var _closeButtonTranslate: Point = this.createCloseButtonTranslate()

    /**
     * Offset of the close button relative to the close button area
     */
    open val closeButtonTranslate: Point
        get() = this._closeButtonTranslate
    // endregion


    // region Close button images
    /**
     * Creating a close button image in the normal state (no hover and no click)
     *
     * @param closeButtonSize Close button size
     *
     * @return Image of the close button in normal state
     */
    protected open fun createCloseButtonImageIdle(closeButtonSize: Dimension): Image {
        return createCloseButtonImage(color = closeButtonDefaultColorIdle, circle = false, width = closeButtonSize.width, height = closeButtonSize.height)
    }

    /**
     * The image of the close button in the normal state (without hovering and pressing); mutable version of [closeButtonImageIdle]
     *
     * By default - [createCloseButtonImageIdle] ([closeButtonSize])
     */
    @Suppress("PropertyName")
    protected open var _closeButtonImageIdle: Image = this.createCloseButtonImageIdle(this.closeButtonSize)

    /**
     * The image of the close button in the normal state (without hovering and pressing)
     */
    open val closeButtonImageIdle: Image
        get() = this._closeButtonImageIdle


    /**
     * Creating a close button image on hover but without pressing
     *
     * @param closeButtonSize Close button size
     *
     * @return Close button image on hover but without pressing
     */
    protected open fun createCloseButtonImageHovered(closeButtonSize: Dimension): Image {
        return createCloseButtonImage(color = closeButtonDefaultColorHovered, circle = true, width = closeButtonSize.width, height = closeButtonSize.height)
    }

    /**
     * An image of a close button on hover, but without pressing; mutable version of closeButtonImageHovered
     *
     * By default - [createCloseButtonImageHovered] ([closeButtonSize])
     */
    @Suppress("PropertyName")
    protected open var _closeButtonImageHovered: Image = this.createCloseButtonImageHovered(this.closeButtonSize)

    /**
     * An image of a close button on hover, but without pressing
     */
    open val closeButtonImageHovered: Image
        get() = this._closeButtonImageHovered


    /**
     * Create close button image on hover and pressing
     *
     * @param closeButtonSize Close button size
     *
     * @return Close button image on hover and pressing
     */
    protected open fun createCloseButtonImagePressed(closeButtonSize: Dimension): Image {
        return createCloseButtonImage(color = closeButtonDefaultColorPressed, circle = true, width = closeButtonSize.width, height = closeButtonSize.height)
    }

    /**
     * Close button image on hover and pressing; mutable version of [closeButtonImagePressed]
     *
     * By default - [createCloseButtonImagePressed] ([closeButtonSize])
     */
    @Suppress("PropertyName")
    protected open var _closeButtonImagePressed: Image = this.createCloseButtonImagePressed(this.closeButtonSize)

    /**
     * Close button image on hover and pressing
     */
    open val closeButtonImagePressed: Image
        get() = this._closeButtonImagePressed
    // endregion


    /**
     * Updates properties:
     *
     * * [_closeButtonSize] = [createCloseButtonSize] ()
     * * [_closeButtonPlaceSize] = [createCloseButtonPlaceSize] ([closeButtonSize])
     * * [_closeButtonTranslate] = [createCloseButtonTranslate] ()
     * * [_closeButtonImageIdle] = [createCloseButtonImageIdle] ([closeButtonSize])
     * * [_closeButtonImageHovered] = [createCloseButtonImageHovered] ([closeButtonSize])
     * * [_closeButtonImagePressed] = [createCloseButtonImagePressed] ([closeButtonSize])
     *
     * After that, it runs the [uiConfigChanged][OreTabPaneUIListener.uiConfigChanged] method for all listeners
     */
    protected open fun updateUiDefaults() {
        this._closeButtonSize = this.createCloseButtonSize()
        this._closeButtonPlaceSize = this.createCloseButtonPlaceSize(this.closeButtonSize)
        this._closeButtonTranslate = this.createCloseButtonTranslate()
        this._closeButtonImageIdle = this.createCloseButtonImageIdle(this.closeButtonSize)
        this._closeButtonImageHovered = this.createCloseButtonImageHovered(this.closeButtonSize)
        this._closeButtonImagePressed = this.createCloseButtonImagePressed(this.closeButtonSize)
        this._dropLocationColor = this.createDropLocationColor()

        this.forEachListener { it.uiConfigChanged(this) }
    }


    // region Listeners
    /**
     * A set of listeners; mutable version of [listeners]
     */
    @Suppress("PropertyName")
    protected open val _listeners: MutableSet<OreTabPaneUIListener> = HashSet()

    /**
     * A set of listeners
     */
    @Suppress("unused")
    open val listeners: Set<OreTabPaneUIListener> = Collections.unmodifiableSet(this._listeners)

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
                OreSwingExtUtils.handle(e)
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
     * Creating a drop location drawing color
     *
     * By default - [dropLocationDefaultColor]
     *
     * @return Drop location drawing color
     */
    protected open fun createDropLocationColor(): Color = dropLocationDefaultColor

    /**
     * Drop location drawing color; mutable version of [dropLocationColor]
     *
     * By default - [createDropLocationColor] ()
     */
    @Suppress("PropertyName")
    protected open var _dropLocationColor: Color = createDropLocationColor()

    /**
     * Drop location drawing color
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


    init {
        store(this)
    }
}
