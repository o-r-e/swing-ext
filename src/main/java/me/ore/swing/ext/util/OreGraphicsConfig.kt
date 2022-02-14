package me.ore.swing.ext.util

import java.awt.*


/**
 * Configuration which can be [applied][applyTo] to any [Graphics] object
 *
 * @param color If is not `null`, then will be passed to [Graphics.setColor]
 * @param font If is not `null`, then will be passed to [Graphics.setFont]
 * @param paintMode If is `true`, then [Graphics.setPaintMode] will be called
 * @param xorModeColor If is not `null`, then will be passed to [Graphics.setXORMode]
 * @param clipRect If is not `null`, then its [x][Rectangle.x], [y][Rectangle.y], [width][Rectangle.width], [height][Rectangle.height] will be passed to [Graphics.clipRect]
 * @param clip If is not `null`, then will be passed to [Graphics.setClip]
 */
open class OreGraphicsConfig(
    open var color: Color? = null,
    open var font: Font? = null,
    open var paintMode: Boolean = false,
    open var xorModeColor: Color? = null,
    open var clipRect: Rectangle? = null,
    open var clip: Shape? = null
) {
    /**
     * Apply current config to [target] in this order:
     *
     * 1. If [color] is not equal to `null`, it is set to `target`.[color][Graphics.setColor];
     * 1. If [font] is not equal to `null`, it is set to `target`.[font][Graphics.setFont];
     * 1. If [paintMode] is `true`, `target`.[setPaintMode][Graphics.setPaintMode] is called;
     * 1. If [xorModeColor] is not `null`, it is passed to `target`.[setXORMode][Graphics.setXORMode];
     * 1. If [clipRect] is not equal to `null`, its [x][Rectangle.x], [y][Rectangle.y], [width][Rectangle.width], [height][Rectangle.height] properties
     * are passed to ` target`.[clipRect][Graphics.clipRect]
     * 1. If [clip] is not `null`, it is passed to `target`.[setClip][Graphics.setClip]
     *
     * @param target Graphics to which the current configuration should be applied
     */
    open fun applyTo(target: Graphics) {
        this.color?.let { target.color = it }
        this.font?.let { target.font = it }
        if (this.paintMode) { target.setPaintMode() }
        this.xorModeColor?.let { target.setXORMode(it) }
        this.clipRect?.let { target.clipRect(it.x, it.y, it.width, it.height) }
        this.clip?.let { target.clip = it }
    }

    @Suppress("DuplicatedCode")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is OreGraphicsConfig) return false

        if (color != other.color) return false
        if (font != other.font) return false
        if (paintMode != other.paintMode) return false
        if (xorModeColor != other.xorModeColor) return false
        if (clipRect != other.clipRect) return false
        if (clip != other.clip) return false

        return true
    }

    override fun hashCode(): Int {
        var result = color?.hashCode() ?: 0
        result = 31 * result + (font?.hashCode() ?: 0)
        result = 31 * result + paintMode.hashCode()
        result = 31 * result + (xorModeColor?.hashCode() ?: 0)
        result = 31 * result + (clipRect?.hashCode() ?: 0)
        result = 31 * result + (clip?.hashCode() ?: 0)
        return result
    }
}
