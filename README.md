# VoxLib

A Fabric and NeoForge library mod for Minecraft that provides Kotlin utilities for creating, combining, rotating, simplifying, and debugging `VoxelShape`s.

[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green)](https://modrinth.com/mod/voxlib/versions)
[![Fabric and NeoForge](https://img.shields.io/badge/Loaders-Fabric%20%2B%20NeoForge-blue)](https://github.com/Mystery2099/VoxLib/wiki/Installation)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple)](https://github.com/Mystery2099/VoxLib/wiki)
[![Documentation](https://img.shields.io/badge/Docs-GitHub%20Wiki-black)](https://github.com/Mystery2099/VoxLib/wiki)

VoxLib takes some of the repetitive work out of Minecraft's shape API. The helpers keep block shape code shorter and easier to read, while vanilla `VoxelShape` operations still handle the geometry underneath.

## What does it actually do?

- Creates cuboids with block-model coordinates from 0 to 16.
- Combines shapes with `+`, unions, Boolean operations, and conditional assembly.
- Rotates and flips directional shapes.
- Provides common slabs, pillars, tables, chairs, fences, and stairs.
- Caches repeated transformations and unions with bounded caches.
- Builds approximate, lower-detail outline shapes when exact geometry is unnecessary.
- Draws targeted outline and collision overlays during development.

## Quick example

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateLeft

val table = createCuboidShape(0, 15, 0, 16, 16, 16) +
    createCuboidShape(1, 0, 1, 3, 15, 3) +
    createCuboidShape(13, 0, 1, 15, 15, 3) +
    createCuboidShape(1, 0, 13, 3, 15, 15) +
    createCuboidShape(13, 0, 13, 15, 15, 15)

val tableFacingWest = table.rotateLeft()
```

Keep fixed shapes in a top-level property, companion object, or static field so Minecraft does not rebuild them on every shape query.

Follow the [getting started guide](https://github.com/Mystery2099/VoxLib/wiki/Getting-Started) to add a shape to a block. Java users should read [Using VoxLib from Java](https://github.com/Mystery2099/VoxLib/wiki/Using-VoxLib-from-Java) for the JVM syntax used to call Kotlin `object` APIs.

## Installation

Choose a release that matches your Minecraft version from the [version matrix](https://github.com/Mystery2099/VoxLib/wiki#version-matrix). For Fabric, the current release is available from Modrinth Maven:

```gradle
repositories {
    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = "https://api.modrinth.com/maven"
            }
        }
        filter {
            includeGroup "maven.modrinth"
        }
    }
}

dependencies {
    modImplementation "maven.modrinth:voxlib:VERSION"
}
```

Replace `VERSION` with a release from [Modrinth](https://modrinth.com/mod/voxlib/versions). The [installation guide](https://github.com/Mystery2099/VoxLib/wiki/Installation) has NeoForge coordinates, GitHub Packages and CurseForge setup, runtime dependencies, and older Minecraft versions.

## Documentation

| Topic | Guide |
| --- | --- |
| First block | [Getting started](https://github.com/Mystery2099/VoxLib/wiki/Getting-Started) |
| Dependency setup | [Installation](https://github.com/Mystery2099/VoxLib/wiki/Installation) |
| Cuboids and coordinates | [Creating shapes](https://github.com/Mystery2099/VoxLib/wiki/Creating-Shapes) |
| Unions and conditional assembly | [Combining shapes](https://github.com/Mystery2099/VoxLib/wiki/Combining-Shapes) |
| Directional geometry | [Rotating and flipping shapes](https://github.com/Mystery2099/VoxLib/wiki/Rotating-and-Flipping-Shapes) |
| Built-in factories | [Common shapes](https://github.com/Mystery2099/VoxLib/wiki/Common-Shapes) |
| Approximate outlines | [Simplifying shapes](https://github.com/Mystery2099/VoxLib/wiki/Simplifying-Shapes) |
| Cache behavior | [Caching and performance](https://github.com/Mystery2099/VoxLib/wiki/Caching-and-Performance) |
| In-game overlays and logging | [Debug tools](https://github.com/Mystery2099/VoxLib/wiki/Debug-Tools) |
| Calling the API from Java | [Using VoxLib from Java](https://github.com/Mystery2099/VoxLib/wiki/Using-VoxLib-from-Java) |
| Release notes and upgrades | [Version history and migration](https://github.com/Mystery2099/VoxLib/wiki/Version-History-and-Migration) |

The repository also contains the methodology and commands for its manual [performance benchmarks](docs/PERFORMANCE.md).

## Current requirements

- Minecraft 1.21.1
- Fabric Loader 0.19.5 or newer, or NeoForge 21.1.250 or newer within 21.1.x
- Java 21 or newer at runtime
- Fabric API and Fabric Language Kotlin on Fabric
- Client and dedicated server

Mod Menu is optional on Fabric. It adds the in-game settings screen for debug overlays. NeoForge exposes the settings through its Mods screen.

## Building from source

```shell
./gradlew build
```

The Gradle build daemon needs JDK 25, while the mod toolchain and compiled JARs target Java 21. Build output is written to:

- `fabric/build/libs/voxlib-fabric-VERSION.jar`
- `neoforge/build/libs/voxlib-neoforge-VERSION.jar`

Run a development client or dedicated server with the loader-specific tasks:

| Development task | Fabric | NeoForge |
| --- | --- | --- |
| Client | `./gradlew :fabric:runClient` | `./gradlew :neoforge:runClient` |
| Dedicated server | `./gradlew :fabric:runServer` | `./gradlew :neoforge:runServer` |

The root `runClient` and `runServer` tasks default to Fabric. See the [installation guide](https://github.com/Mystery2099/VoxLib/wiki/Installation#building-from-source) for local Maven publishing and mapping notes.

## Project layout

- `common/src/main` contains the shared geometry, rotation, caching, simplification, initialization, and assets.
- `common/src/client` contains shared settings, configuration, and debug rendering code.
- `fabric` contains Fabric entrypoints, rendering integration, and Mod Menu integration.
- `neoforge` contains NeoForge entrypoints, rendering integration, and config-screen registration.

VoxLib uses Mojang mappings for Minecraft 1.21.1. The common module's JAR is a development artifact, not an installable mod. VoxLib has no Architectury dependency.

## Support

VoxLib is free, and it always will be. Nobody owes me anything for it.

If you still want to support my projects, you can [buy me a coffee](https://buymeacoffee.com/mystery2099). No pressure at all.

## License

VoxLib is available under the [Minecraft Mod Public License 1.0.1](LICENSE).
