package com.github.mystery2099.voxlib.config

import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.gui.components.Button
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import kotlin.math.roundToInt

internal class VoxLibConfigScreen(private val parent: Screen) : Screen(Component.literal("VoxLib Settings")) {
    private var config = VoxLibConfig.get()

    override fun init() {
        super.init()

        val startY = height / 2 - 82
        addRenderableWidget(createDebugModeButton(startY))
        addRenderableWidget(createTargetOutlineButton(startY + 24))
        addRenderableWidget(createTargetCollisionButton(startY + 48))
        addRenderableWidget(createColorButton(startY + 72))
        addRenderableWidget(createAlphaButton(startY + 96))
        addRenderableWidget(createResetButton(startY + 128))
        addRenderableWidget(createDoneButton(startY + 152))
    }

    private fun createDebugModeButton(y: Int): Button =
        button(debugModeText(), y) { widget ->
            update(config.copy(debugModeEnabled = !config.debugModeEnabled))
            widget.message = debugModeText()
        }

    private fun createTargetOutlineButton(y: Int): Button =
        button(targetOutlineText(), y) { widget ->
            update(config.copy(showTargetedOutline = !config.showTargetedOutline))
            widget.message = targetOutlineText()
        }

    private fun createTargetCollisionButton(y: Int): Button =
        button(targetCollisionText(), y) { widget ->
            update(config.copy(showTargetedCollision = !config.showTargetedCollision))
            widget.message = targetCollisionText()
        }

    private fun createColorButton(y: Int): Button =
        button(colorText(), y) { widget ->
            val currentIndex = COLOR_OPTIONS.indexOfFirst { it.value == config.debugShapeColor }
            val nextIndex = (currentIndex + 1).coerceAtLeast(0) % COLOR_OPTIONS.size
            update(config.copy(debugShapeColor = COLOR_OPTIONS[nextIndex].value))
            widget.message = colorText()
        }

    private fun createAlphaButton(y: Int): Button =
        button(alphaText(), y) { widget ->
            val nextAlpha = if (config.debugShapeAlpha >= 1.0f) {
                0.1f
            } else {
                ((config.debugShapeAlpha * 10).roundToInt() + 1) / 10.0f
            }
            update(config.copy(debugShapeAlpha = nextAlpha))
            widget.message = alphaText()
        }

    private fun createResetButton(y: Int): Button =
        button(Component.literal("Reset to Defaults"), y) {
            update(VoxLibConfig.default())
            minecraft?.setScreen(VoxLibConfigScreen(parent))
        }

    private fun createDoneButton(y: Int): Button =
        button(Component.literal("Done"), y) {
            onClose()
        }

    private fun button(
        message: Component,
        y: Int,
        onPress: (Button) -> Unit
    ): Button = Button.builder(message, onPress)
        .bounds(width / 2 - 100, y, 200, 20)
        .build()

    private fun update(newConfig: VoxLibConfig) {
        config = newConfig.normalized()
        VoxLibConfig.update(config)
    }

    private fun debugModeText(): Component =
        Component.literal("Debug Mode: ${if (config.debugModeEnabled) "ON" else "OFF"}")

    private fun targetOutlineText(): Component =
        Component.literal("Target Outline: ${if (config.showTargetedOutline) "ON" else "OFF"}")

    private fun targetCollisionText(): Component =
        Component.literal("Target Collision: ${if (config.showTargetedCollision) "ON" else "OFF"}")

    private fun colorText(): Component {
        val name = COLOR_OPTIONS.firstOrNull { it.value == config.debugShapeColor }?.name ?: "Custom"
        return Component.literal("Color: $name")
    }

    private fun alphaText(): Component =
        Component.literal("Alpha: ${(config.debugShapeAlpha * 100).roundToInt()}%")

    override fun render(context: GuiGraphics, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(context)
        context.drawString(
            font,
            title,
            (width - font.width(title)) / 2,
            20,
            0xFFFFFF,
            false
        )
        super.render(context, mouseX, mouseY, delta)
    }

    override fun onClose() {
        minecraft?.setScreen(parent)
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
