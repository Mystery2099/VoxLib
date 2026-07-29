package com.github.mystery2099.voxlib.config

import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import kotlin.math.roundToInt

@Environment(EnvType.CLIENT)
internal class VoxLibConfigScreen(private val parent: Screen) : Screen(Text.literal("VoxLib Settings")) {
    private var config = VoxLibConfig.get()

    override fun init() {
        super.init()

        addDrawableChild(createDebugModeButton(height / 2 - 70))
        addDrawableChild(createColorButton(height / 2 - 45))
        addDrawableChild(createAlphaButton(height / 2 - 20))
        addDrawableChild(createResetButton(height / 2 + 20))
        addDrawableChild(createDoneButton(height / 2 + 45))
    }

    private fun createDebugModeButton(y: Int): ButtonWidget =
        button(debugModeText(), y) { widget ->
            update(config.copy(debugModeEnabled = !config.debugModeEnabled))
            widget.message = debugModeText()
        }

    private fun createColorButton(y: Int): ButtonWidget =
        button(colorText(), y) { widget ->
            val currentIndex = COLOR_OPTIONS.indexOfFirst { it.value == config.debugShapeColor }
            val nextIndex = (currentIndex + 1).coerceAtLeast(0) % COLOR_OPTIONS.size
            update(config.copy(debugShapeColor = COLOR_OPTIONS[nextIndex].value))
            widget.message = colorText()
        }

    private fun createAlphaButton(y: Int): ButtonWidget =
        button(alphaText(), y) { widget ->
            val nextAlpha = if (config.debugShapeAlpha >= 1.0f) {
                0.1f
            } else {
                ((config.debugShapeAlpha * 10).roundToInt() + 1) / 10.0f
            }
            update(config.copy(debugShapeAlpha = nextAlpha))
            widget.message = alphaText()
        }

    private fun createResetButton(y: Int): ButtonWidget =
        button(Text.literal("Reset to Defaults"), y) {
            update(VoxLibConfig.default())
            client?.setScreen(VoxLibConfigScreen(parent))
        }

    private fun createDoneButton(y: Int): ButtonWidget =
        button(Text.literal("Done"), y) {
            close()
        }

    private fun button(
        message: Text,
        y: Int,
        onPress: (ButtonWidget) -> Unit
    ): ButtonWidget = ButtonWidget.builder(message, onPress)
        .dimensions(width / 2 - 100, y, 200, 20)
        .build()

    private fun update(newConfig: VoxLibConfig) {
        config = newConfig.normalized()
        VoxLibConfig.update(config)
    }

    private fun debugModeText(): Text =
        Text.literal("Debug Mode: ${if (config.debugModeEnabled) "ON" else "OFF"}")

    private fun colorText(): Text {
        val name = COLOR_OPTIONS.firstOrNull { it.value == config.debugShapeColor }?.name ?: "Custom"
        return Text.literal("Color: $name")
    }

    private fun alphaText(): Text =
        Text.literal("Alpha: ${(config.debugShapeAlpha * 100).roundToInt()}%")

    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        textRenderer.draw(
            matrices,
            title,
            (width - textRenderer.getWidth(title)) / 2.0f,
            20.0f,
            0xFFFFFF
        )
        super.render(matrices, mouseX, mouseY, delta)
    }

    override fun close() {
        client?.setScreen(parent)
    }

    private data class ColorOption(val name: String, val value: Int)

    private companion object {
        val COLOR_OPTIONS = listOf(
            ColorOption("Red", 0xFFFF0000.toInt()),
            ColorOption("Green", 0xFF00FF00.toInt()),
            ColorOption("Blue", 0xFF0000FF.toInt()),
            ColorOption("Yellow", 0xFFFFFF00.toInt()),
            ColorOption("White", 0xFFFFFFFF.toInt())
        )
    }
}
