package me.ore.swing.ext

import java.awt.*
import java.awt.dnd.DragSource
import java.awt.dnd.DragSourceAdapter
import java.awt.dnd.DragSourceDragEvent
import java.awt.dnd.DragSourceDropEvent
import java.util.*
import javax.swing.ImageIcon
import javax.swing.JLabel


/**
 * Renderer for the image moving with the mouse cursor during the Drop-and-Drop process
 *
 * For the renderer to work,
 * the [init] method must be called at the beginning of the application,
 * and the [image] property must be set before the start of the Drag-and-Drop process.
 *
 * This object is NOT thread-safe, all calls must be inside [EventQueue.dispatchThread]
 */
object OreDnDImageRender {
    /**
     * Which side of the cursor to place [image]
     */
    enum class Side {
        /**
         * The image is positioned above the mouse cursor
         */
        NORTH,

        /**
         * The image is positioned above and to the right of the mouse cursor
         */
        NORTH_EAST,

        /**
         * The image is positioned to the right of the mouse cursor
         */
        EAST,

        /**
         * The image is positioned below and to the right of the mouse cursor
         */
        SOUTH_EAST,

        /**
         * The image is positioned below the mouse cursor
         */
        SOUTH,

        /**
         * The image is positioned below and to the left of the mouse cursor
         */
        SOUTH_WEST,

        /**
         * The image is positioned to the left of the mouse cursor
         */
        WEST,

        /**
         * The image is positioned above and to the left of the mouse cursor
         */
        NORTH_WEST
    }

    /**
     * The default order in which the [image] is positioned relative to the mouse cursor so that the [image] does not cross the edge of the screen
     */
    @Suppress("MemberVisibilityCanBePrivate")
    val DEFAULT_SIDE_ORDER: Array<Side> = arrayOf(Side.SOUTH_EAST, Side.SOUTH, Side.EAST, Side.SOUTH_WEST, Side.NORTH_EAST, Side.WEST, Side.NORTH, Side.NORTH_WEST)


    /**
     * Map "window-owner -> window with image"
     */
    private val OWNER_WINDOW_TO_DRAGGED_WINDOW_MAP = WeakHashMap<Window, Window>()

    /**
     * Getting or creating a draggable window for the owner window
     *
     * @param ownerWindow Owner window
     *
     * @return Window with draggable image
     */
    private fun getDraggedWindowFor(ownerWindow: Window): Window {
        return OWNER_WINDOW_TO_DRAGGED_WINDOW_MAP[ownerWindow]
            ?: run {
                val window = Window(ownerWindow)
                OWNER_WINDOW_TO_DRAGGED_WINDOW_MAP[ownerWindow] = window
                window.layout = BorderLayout()
                window.type = Window.Type.UTILITY
                window
            }
    }


    // region Component & its window
    /**
     * Drag-and-Drop event source component
     *
     * On change triggers [componentWindow] change
     */
    private var component: Component? = null
        set(new) {
            if (field != new) {
                field = new

                componentWindow = new?.let { OreSwingExt.getWindowOf(new) }
            }
        }

    /**
     * Window containing [component]
     *
     * Runs [updateConfig] on change
     */
    private var componentWindow: Window? = null
        set(new) {
            if (field != new) {
                field = new
                updateConfig()
            }
        }
    // endregion


    // region Mouse position & screen bounds
    /**
     * Mouse cursor coordinates on the screen
     *
     * On change triggers [mousePositionAndScreenBounds] change
     */
    private var mousePosition: Point? = null
        set(new) {
            if (field != new) {
                field = new

                mousePositionAndScreenBounds = new?.let { mousePosition ->
                    val bounds = run screenBounds@{
                        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
                            ?: return@screenBounds null

                        val screenDevices = environment.screenDevices?.takeIf { it.isNotEmpty() }
                            ?: return@screenBounds null

                        val screensBounds = screenDevices.map {
                            val configuration = it.defaultConfiguration
                                ?: return@map null

                            val bounds = configuration.bounds
                                ?: return@map null

                            if (!bounds.contains(mousePosition)) {
                                return@map null
                            }

                            bounds
                        }
                            .filterNotNull()

                        screensBounds.firstOrNull()
                    }

                    bounds?.let {
                        mousePosition to bounds
                    }
                }
            }
        }

    /**
     * Pair "mouse coordinates + screen borders" (the screen in which the mouse cursor is located)
     *
     * Runs [updateConfig] on change
     */
    private var mousePositionAndScreenBounds: Pair<Point, Rectangle>? = null
        set(new) {
            if (field != new) {
                field = new
                updateConfig()
            }
        }
    // endregion


    // region Image, image offset, image side order
    /**
     * An image that will move with the mouse cursor during the drag-and-drop process
     *
     * _Note: when the Drag-and-Drop process terminates, this property is set to `null`_
     */
    var image: Image? = null
        set(new) {
            if (field != new) {
                field = new

                imageLabel = new?.let {
                    val icon = ImageIcon(it)
                    JLabel(icon)
                }
            }
        }

    /**
     * [JLabel] that contains [image]
     *
     * Changes when [image] changes
     *
     * Runs [updateConfig] on change
     */
    private var imageLabel: JLabel? = null
        set(new) {
            if (field != new) {
                field = new
                updateConfig()
            }
        }

    /**
     * Indent [image] from the mouse cursor along the X axis, in pixels
     *
     * By default - `10`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var imageOffsetX: Int = 10
        set(new) {
            if (field != new) {
                field = new
                updateConfig()
            }
        }

    /**
     * Indent [image] from the mouse cursor along the Y axis, in pixels
     *
     * By default - `10`
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var imageOffsetY: Int = 10
        set(new) {
            if (field != new) {
                field = new
                updateConfig()
            }
        }

    /**
     * The order in which the [image] is positioned relative to the mouse cursor so that the [image] does not cross the edge of the screen
     *
     * By default - [DEFAULT_SIDE_ORDER]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var imageSideOrder: Array<Side> = DEFAULT_SIDE_ORDER
        set(new) {
            if (!field.contentEquals(new)) {
                field = new
                updateConfig()
            }
        }
    // endregion


    // region Config
    /**
     * Settings for displaying [image]
     *
     * @param componentWindow See [OreDnDImageRender.componentWindow]
     * @param mousePosition See [OreDnDImageRender.mousePositionAndScreenBounds]
     * @param screenBounds See [OreDnDImageRender.mousePositionAndScreenBounds]
     * @param imageLabel See [OreDnDImageRender.imageLabel]
     * @param imageOffsetX See [OreDnDImageRender.imageOffsetX]
     * @param imageOffsetY See [OreDnDImageRender.imageOffsetY]
     * @param sides See [OreDnDImageRender.imageSideOrder]
     */
    @Suppress("DuplicatedCode")
    private data class Config(
        val componentWindow: Window,
        val mousePosition: Point,
        val screenBounds: Rectangle,
        val imageLabel: JLabel,
        val imageOffsetX: Int,
        val imageOffsetY: Int,
        val sides: Array<Side>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Config

            if (componentWindow != other.componentWindow) return false
            if (mousePosition != other.mousePosition) return false
            if (imageLabel != other.imageLabel) return false
            if (imageOffsetX != other.imageOffsetX) return false
            if (imageOffsetY != other.imageOffsetY) return false
            if (!sides.contentEquals(other.sides)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = componentWindow.hashCode()
            result = 31 * result + mousePosition.hashCode()
            result = 31 * result + imageLabel.hashCode()
            result = 31 * result + imageOffsetX
            result = 31 * result + imageOffsetY
            result = 31 * result + sides.contentHashCode()
            return result
        }
    }


    /**
     * Settings for displaying [image]
     *
     * Runs [updateWindow] on change
     */
    private var config: Config? = null
        set(new) {
            if (field != new) {
                field = new
                updateWindow()
            }
        }

    /**
     * Fired when [componentWindow], [mousePositionAndScreenBounds], [imageLabel], [imageOffsetX], [imageOffsetY] and/or [imageSideOrder] change,
     * stores these values in [config]
     *
     * If one or more of the [componentWindow], [mousePositionAndScreenBounds], [imageLabel] properties are `null`,
     * `null` is also stored in [config]
     */
    private fun updateConfig() {
        this.config = run newConfig@ {
            val componentWindow = componentWindow
                ?: return@newConfig null

            val (mousePosition, screenBounds) = mousePositionAndScreenBounds
                ?: return@newConfig null

            val imageLabel = imageLabel
                ?: return@newConfig null

            Config(
                componentWindow = componentWindow,
                mousePosition = mousePosition,
                screenBounds = screenBounds,
                imageLabel = imageLabel,
                imageOffsetX = imageOffsetX,
                imageOffsetY = imageOffsetY,
                sides = imageSideOrder
            )
        }
    }
    // endregion


    // region Window
    /**
     * If set to `true`, the [dispose][Window.dispose] method is called when the window with [image] is hidden
     */
    @Suppress("MemberVisibilityCanBePrivate")
    var disposeDraggedWindowOnHide = false

    /**
     * Window with draggable [image]
     *
     * When changed, the old window is hidden;
     * if [disposeDraggedWindowOnHide] is `true`, the old window's [dispose][Window.dispose] method is called
     */
    private var window: Window? = null
        set(new) {
            if (field != new) {
                val old = field
                field = new

                old?.let {
                    it.isVisible = false
                    if (this.disposeDraggedWindowOnHide) {
                        it.dispose()
                    }
                }
            }
        }


    /**
     * Getting the window coordinate along the X axis to the left of the mouse cursor
     *
     * @param mouseX Coordinate of mouse cursor along X axis
     * @param windowWidth Width of window with [image]
     * @param offsetX Window offset from the mouse cursor along the X axis
     *
     * @return Coordinate of window along X axis
     */
    private fun windowPositionXWest(mouseX: Int, windowWidth: Int, offsetX: Int): Int = mouseX - windowWidth - offsetX

    /**
     * Getting the window coordinate along the X axis with the coincidence of the window center with the mouse cursor
     *
     * @param mouseX Coordinate of mouse cursor along X axis
     * @param windowWidth Width of window with [image]
     *
     * @return Coordinate of window along X axis
     */
    private fun windowPositionXCenter(mouseX: Int, windowWidth: Int): Int = mouseX - (windowWidth / 2)

    /**
     * Getting the window coordinate along the X axis to the right of the mouse cursor
     *
     * @param mouseX Coordinate of mouse cursor along X axis
     * @param offsetX Window offset from the mouse cursor along the X axis
     *
     * @return Coordinate of window along X axis
     */
    private fun windowPositionXEast(mouseX: Int, offsetX: Int): Int = mouseX + offsetX

    /**
     * Getting the coordinate of the window along the Y axis from above from the mouse cursor
     *
     * @param mouseY Coordinate of mouse cursor along Y axis
     * @param windowHeight Height of window with [image]
     * @param offsetY Window offset from the mouse cursor along the Y axis
     *
     * @return Coordinate of window along Y axis
     */
    private fun windowPositionYNorth(mouseY: Int, windowHeight: Int, offsetY: Int): Int = mouseY - windowHeight - offsetY

    /**
     * Getting the coordinate of the window along the Y axis with the coincidence of the center of the window with the mouse cursor
     *
     * @param mouseY Coordinate of mouse cursor along Y axis
     * @param windowHeight Height of window with [image]
     *
     * @return Coordinate of window along Y axis
     */
    private fun windowPositionYCenter(mouseY: Int, windowHeight: Int): Int = mouseY - (windowHeight / 2)

    /**
     * Getting the window coordinate along the Y axis from the bottom of the mouse cursor
     *
     * @param mouseY Coordinate of mouse cursor along Y axis
     * @param offsetY Window offset from the mouse cursor along the Y axis
     *
     * @return Coordinate of window along Y axis
     */
    private fun windowPositionYSouth(mouseY: Int, offsetY: Int): Int = mouseY + offsetY

    /**
     * Getting window coordinates
     *
     * @param mousePosition Mouse cursor coordinates
     * @param windowSize Size of window
     * @param offsetX Window offset from the mouse cursor along the X axis
     * @param offsetY Window offset from the mouse cursor along the Y axis
     * @param side The side on which, relative to the mouse cursor, the window should be located
     *
     * @return Window coordinates
     */
    private fun windowPosition(mousePosition: Point, windowSize: Dimension, offsetX: Int, offsetY: Int, side: Side): Point {
        val mouseX = mousePosition.x
        val mouseY = mousePosition.y
        val windowWidth = windowSize.width
        val windowHeight = windowSize.height

        val result = Point(0, 0)

        when (side) {
            Side.NORTH -> {
                result.x = windowPositionXCenter(mouseX, windowWidth)
                result.y = windowPositionYNorth(mouseY, windowHeight, offsetY)
            }
            Side.NORTH_EAST -> {
                result.x = windowPositionXEast(mouseX, offsetX)
                result.y = windowPositionYNorth(mouseY, windowHeight, offsetY)
            }
            Side.EAST -> {
                result.x = windowPositionXEast(mouseX, offsetX)
                result.y = windowPositionYCenter(mouseY, windowHeight)
            }
            Side.SOUTH_EAST -> {
                result.x = windowPositionXEast(mouseX, offsetX)
                result.y = windowPositionYSouth(mouseY, offsetY)
            }
            Side.SOUTH -> {
                result.x = windowPositionXCenter(mouseX, windowWidth)
                result.y = windowPositionYSouth(mouseY, offsetY)
            }
            Side.SOUTH_WEST -> {
                result.x = windowPositionXWest(mouseX, windowWidth, offsetX)
                result.y = windowPositionYSouth(mouseY, offsetY)
            }
            Side.WEST -> {
                result.x = windowPositionXWest(mouseX, windowWidth, offsetX)
                result.y = windowPositionYCenter(mouseY, windowHeight)
            }
            Side.NORTH_WEST -> {
                result.x = windowPositionXWest(mouseX, windowWidth, offsetX)
                result.y = windowPositionYNorth(mouseY, windowHeight, offsetY)
            }
        }

        return result
    }

    /**
     * Getting window coordinates such that the window will not cross the screen borders
     *
     * @param mousePosition Mouse cursor coordinates
     * @param screenBounds Screen bounds
     * @param windowSize Size of window
     * @param offsetX Window offset from the mouse cursor along the X axis
     * @param offsetY Window offset from the mouse cursor along the Y axis
     * @param sides The order in which the window location is selected
     *
     * @return Window coordinates
     */
    private fun windowPosition(mousePosition: Point, screenBounds: Rectangle, windowSize: Dimension, offsetX: Int, offsetY: Int, sides: Array<Side>): Point {
        var maxIntersectionSquare = 0
        var maxIntersectionPosition: Point? = null

        for (side in sides) {
            val windowPosition = windowPosition(mousePosition, windowSize, offsetX, offsetY, side)
            val windowBounds = Rectangle(windowPosition, windowSize)
            if (screenBounds.contains(windowBounds)) {
                return windowPosition
            }

            val intersection = screenBounds.intersection(windowBounds)
            val intersectionSquare = (intersection.width * intersection.height)
            if ((maxIntersectionPosition == null) || (intersectionSquare > maxIntersectionSquare)) {
                maxIntersectionSquare = intersectionSquare
                maxIntersectionPosition = windowPosition
            }
        }

        return maxIntersectionPosition ?: windowPosition(mousePosition, windowSize, offsetX, offsetY, Side.SOUTH_EAST)
    }


    /**
     * Update window with [image] to match [config] settings
     *
     * If [config] is `null`, the window will be hidden
     */
    private fun updateWindow() {
        val config = config
        if (config == null) {
            window = null
        } else {
            val window = this.getDraggedWindowFor(config.componentWindow)
            this.window = window

            val imageLabel = config.imageLabel

            val components = window.components
            if ((components.size != 1) || (components[0] != imageLabel)) {
                window.removeAll()
                window.add(imageLabel, BorderLayout.CENTER)
                window.pack()
            }

            val windowSize = window.size

            val mousePosition = config.mousePosition
            val screenBounds = config.screenBounds
            val offsetX = config.imageOffsetX
            val offsetY = config.imageOffsetY
            val sides = config.sides

            val windowPosition = windowPosition(mousePosition, screenBounds, windowSize, offsetX, offsetY, sides)
            window.location = windowPosition

            if (!window.isVisible) {
                window.isVisible = true
            }
        }
    }
    // endregion


    // region Drag-and-Drop listener and methods
    /**
     * Listener registered to [DragSource.getDefaultDragSource] () when calling the [init] method
     *
     * [DnDListener.dragDropEnd] calls [OreDnDImageRender.dragDropEnd]
     *
     * [DnDListener.dragMouseMoved] calls [OreDnDImageRender.dragMouseMoved]
     */
    private object DnDListener: DragSourceAdapter() {
        override fun dragDropEnd(event: DragSourceDropEvent?) {
            @Suppress("RemoveRedundantQualifierName")
            OreDnDImageRender.dragDropEnd()
        }

        override fun dragMouseMoved(event: DragSourceDragEvent?) {
            event ?: return
            OreDnDImageRender.dragMouseMoved(event)
        }
    }


    /**
     * This method is invoked to signify that the Drag and Drop operation is complete
     *
     * [mousePosition] and [component] are setting to `null`
     */
    private fun dragDropEnd() {
        mousePosition = null
        component = null
        image = null
    }

    /**
     * Called whenever the mouse is moved during a drag operation
     *
     * [component] is setting to [event].[dragSourceContext][DragSourceDragEvent.getDragSourceContext]?.[component][java.awt.dnd.DragSourceContext.getComponent]
     *
     * [mousePosition] is setting to [event].[location][DragSourceDragEvent.getLocation]
     *
     * @param event The DragSourceDragEvent
     */
    private fun dragMouseMoved(event: DragSourceDragEvent) {
        component = event.dragSourceContext?.component
        mousePosition = event.location
    }
    // endregion


    /**
     * Initialization
     *
     * Registering a listener on the object returned by the [DragSource.getDefaultDragSource] () method
     */
    fun init() {
        DragSource.getDefaultDragSource().addDragSourceListener(DnDListener)
        DragSource.getDefaultDragSource().addDragSourceMotionListener(DnDListener)
    }
}
