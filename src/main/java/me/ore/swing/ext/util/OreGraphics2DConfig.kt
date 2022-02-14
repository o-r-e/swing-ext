package me.ore.swing.ext.util

import java.awt.*


/**
 * Configuration which can be [applied][applyTo] to any [Graphics] object;
 * some properties will be applied only if target object is [Graphics2D]
 *
 * @param color If is not `null`, then will be passed to [Graphics.setColor]
 * @param background __(for [Graphics2D])__ if is not `null`, then will be passed to [Graphics2D.setBackground]
 * @param stroke __(for [Graphics2D])__ if is not `null`, then will be passed to [Graphics2D.setStroke]
 * @param paint __(for [Graphics2D])__ if is not `null`, then will be passed to [Graphics2D.setPaint]
 * @param font If is not `null`, then will be passed to [Graphics.setFont]
 * @param paintMode If is `true`, then [Graphics.setPaintMode] will be called
 * @param xorModeColor If is not `null`, then will be passed to [Graphics.setXORMode]
 * @param composite __(for [Graphics2D])__ if is not `null`, then will be passed to [Graphics2D.setComposite]
 * @param clipRect If is not `null`, then its [x][Rectangle.x], [y][Rectangle.y], [width][Rectangle.width], [height][Rectangle.height] will be passed to [Graphics.clipRect]
 * @param clip If is not `null`, then will be passed to [Graphics.setClip]
 * @param renderingHints __(for [Graphics2D])__ if is not `null`, then will be passed to [Graphics2D.setRenderingHints]
 */
open class OreGraphics2DConfig(
    color: Color? = null,
    open var background: Color? = null,
    open var stroke: Stroke? = null,
    open var paint: Paint? = null,
    font: Font? = null,
    paintMode: Boolean = false,
    xorModeColor: Color? = null,
    open var composite: Composite? = null,
    clipRect: Rectangle? = null,
    clip: Shape? = null,
    open var renderingHints: Map<out Any?, Any?>? = null
): OreGraphicsConfig(color, font, paintMode, xorModeColor, clipRect, clip) {
    /**
     * Apply current config to [target] in this order:
     *
     * 1. If [color] is not `null`, it is set to `target`.[color][Graphics.setColor];
     * 1. If [font] is not `null`, it is set to `target`.[font][Graphics.setFont];
     * 1. If [paintMode] is `true`, `target`.[setPaintMode][Graphics.setPaintMode] is called;
     * 1. If [xorModeColor] is not `null`, it is passed to `target`.[setXORMode][Graphics.setXORMode];
     * 1. If [clipRect] is not `null`, its [x][Rectangle.x], [y][Rectangle.y], [width][Rectangle.width], [height][Rectangle.height] properties
     * are passed to ` target`.[clipRect][Graphics.clipRect]
     * 1. If [clip] is not `null`, it is passed to `target`.[setClip][Graphics.setClip]
     *
     * If `target` is an object of class [Graphics2D], then additional actions are applied:
     *
     * 1. If [background] is not `null`, it is set to `target`.[background][Graphics2D.setBackground]
     * 1. If [background] is not `null`, it is set to `target`.[stroke][Graphics2D.setStroke]
     * 1. If [background] is not `null`, it is set to `target`.[paint][Graphics2D.setPaint]
     * 1. If [background] is not `null`, it is set to `target`.[composite][Graphics2D.setComposite]
     * 1. If [background] is not `null`, it is passed to `target`.[Graphics2D.setRenderingHints]
     *
     * @param target Graphics to which the current configuration should be applied
     */
    override fun applyTo(target: Graphics) {
        super.applyTo(target)

        if (target is Graphics2D) {
            this.background?.let { target.background = it }
            this.stroke?.let { target.stroke = it }
            this.paint?.let { target.paint = it }
            this.composite?.let { target.composite = it }
            this.renderingHints?.let { target.setRenderingHints(it) }
        }
    }


    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OreGraphics2DConfig) return false
        if (!super.equals(other)) return false

        if (background != other.background) return false
        if (stroke != other.stroke) return false
        if (paint != other.paint) return false
        if (composite != other.composite) return false
        if (renderingHints != other.renderingHints) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (background?.hashCode() ?: 0)
        result = 31 * result + (stroke?.hashCode() ?: 0)
        result = 31 * result + (paint?.hashCode() ?: 0)
        result = 31 * result + (composite?.hashCode() ?: 0)
        result = 31 * result + (renderingHints?.hashCode() ?: 0)
        return result
    }
}
