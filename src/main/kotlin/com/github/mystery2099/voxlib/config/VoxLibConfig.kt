package com.github.mystery2099.voxlib.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.gui.screen.Screen
import org.slf4j.LoggerFactory
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

/**
 * Configuration class for VoxLib debug settings.
 * Provides validated JSON file persistence and ModMenu integration.
 *
 * @param debugModeEnabled Whether debug mode is enabled
 * @param debugShapeColor Color used for debug shape rendering (ARGB format)
 * @param debugShapeAlpha Transparency of debug shapes (0.0-1.0)
 */
@Environment(EnvType.CLIENT)
data class VoxLibConfig(
    var debugModeEnabled: Boolean = false,
    var debugShapeColor: Int = DEFAULT_COLOR,
    var debugShapeAlpha: Float = DEFAULT_ALPHA
) {
    fun normalized(): VoxLibConfig = copy(
        debugShapeAlpha = debugShapeAlpha.coerceIn(MIN_ALPHA, MAX_ALPHA)
    )

    companion object {
        private const val CONFIG_FILE_NAME = "voxlib-client.json"
        internal const val DEFAULT_COLOR = 0xFFFF0000.toInt()
        internal const val DEFAULT_ALPHA = 0.4f
        private const val MIN_ALPHA = 0.0f
        private const val MAX_ALPHA = 1.0f
        private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        private val logger = LoggerFactory.getLogger("VoxLib/Config")

        @Volatile
        private var instance: VoxLibConfig? = null

        /**
         * Gets or creates the singleton config instance.
         */
        @Synchronized
        fun getOrCreate(): VoxLibConfig {
            instance?.let { return it }
            return loadFromFile().also { instance = it }
        }

        /**
         * Gets the config instance.
         */
        fun get(): VoxLibConfig = getOrCreate()

        /**
         * Sets the config instance.
         */
        fun update(config: VoxLibConfig) {
            val normalizedConfig = config.normalized()
            instance = normalizedConfig
            saveToFile(normalizedConfig)
        }

        /**
         * Creates the default configuration instance.
         */
        fun default(): VoxLibConfig = VoxLibConfig()

        /**
         * Creates a config screen for ModMenu integration.
         *
         * @param parent The parent screen
         * @return A config screen
         */
        fun createConfigScreen(parent: Screen): Screen {
            return VoxLibConfigScreen(parent)
        }

        private fun getConfigFile(): Path =
            FabricLoader.getInstance().configDir.resolve(CONFIG_FILE_NAME)

        private fun loadFromFile(): VoxLibConfig {
            val file = getConfigFile()
            return if (Files.exists(file)) {
                try {
                    gson.fromJson(Files.readString(file), VoxLibConfig::class.java)
                        ?.normalized()
                        ?: default()
                } catch (e: Exception) {
                    logger.warn("Unable to read {}; using defaults", file, e)
                    default()
                }
            } else {
                default().also { saveToFile(it) }
            }
        }

        private fun saveToFile(config: VoxLibConfig) {
            val file = getConfigFile()
            val temporaryFile = file.resolveSibling("$CONFIG_FILE_NAME.tmp")
            try {
                Files.createDirectories(file.parent)
                Files.writeString(temporaryFile, gson.toJson(config))
                try {
                    Files.move(
                        temporaryFile,
                        file,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING
                    )
                } catch (_: AtomicMoveNotSupportedException) {
                    Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (e: Exception) {
                logger.error("Unable to save {}", file, e)
                runCatching { Files.deleteIfExists(temporaryFile) }
            }
        }
    }
}
