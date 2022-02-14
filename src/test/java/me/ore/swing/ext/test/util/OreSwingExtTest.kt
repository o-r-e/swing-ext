package me.ore.swing.ext.test.util

import me.ore.swing.ext.OreSwingExt
import javax.swing.UIManager


object OreSwingExtTest {
    @Suppress("unused")
    enum class LafChoice(val className: String) {
        MOTIF("com.sun.java.swing.plaf.motif.MotifLookAndFeel"),
        METAL("javax.swing.plaf.metal.MetalLookAndFeel"),
        NIMBUS("javax.swing.plaf.nimbus.NimbusLookAndFeel"),
        SYSTEM(UIManager.getSystemLookAndFeelClassName())
    }

    fun applyLaf(lafChoice: LafChoice) {
        try {
            UIManager.setLookAndFeel(lafChoice.className)
        } catch (e: Exception) {
            OreSwingExt.handle(e)
        }
    }
}
