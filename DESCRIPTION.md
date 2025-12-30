# VoxLib

A Minecraft Fabric library mod that provides utilities for manipulating, creating, and rotating voxel shapes in your code!

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19.4-green)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)

## What is VoxLib?

VoxLib is a lightweight Fabric library that simplifies working with Minecraft's `VoxelShape` API. It provides intuitive extensions and utilities for creating, combining, transforming, and optimizing voxel shapes—perfect for custom blocks and items that need precise collision or outline shapes.

## Features

### Create
```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape

// Create cuboid shapes with intuitive syntax
val slab = createCuboidShape(0, 0, 0, 16, 8, 16)
```

### Combine
```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus

// Operator overloading for easy shape combination
val tableShape = base + post + top
```

### Transform
```kotlin
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateLeft
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flip

// Rotate and flip shapes easily
val eastFacing = northFacing.rotateLeft()
val opposite = shape.flip()
```

### Optimize
- Caffeine-based caching (500 entries, 10-min expiration)
- Shape simplification utilities
- Divide-and-conquer union algorithms

### Debug (Client)
- In-world shape visualization
- Shape information logging
- Shape comparison tools

### Pre-defined Common Shapes
```kotlin
import com.github.mystery2099.voxlib.shapes.CommonShapes

val slab = CommonShapes.createSlab(8)
val table = CommonShapes.createTable()
val chair = CommonShapes.createChair()
val stairs = CommonShapes.createStairs(Direction.NORTH)
val fencePost = CommonShapes.createFencePost()
val fenceConnections = CommonShapes.createFenceConnections(north = true, east = true)
```

## API Overview

| Category | Functions |
|----------|-----------|
| **Creation** | `createCuboidShape()`, `CommonShapes` factory methods |
| **Combination** | `+`, `+=`, `and`, `union()`, `unifyWith()` |
| **Transformation** | `rotateLeft()`, `rotateRight()`, `flip()`, `flipVertical()`, `flipZ()`, `rotate()` |
| **Simplification** | `simplifyForOutline()`, `toBoundingBoxShape()` |
| **DSL Building** | `appendShapes { }` for conditional assembly |
| **Debug** | `renderShape()`, `logShapeInfo()`, `compareShapes()` |

## Adding VoxLib as a Dependency

### Using JitPack (Easiest)
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation 'com.github.Mystery2099:VoxLib:v1.4.0'
}
```

### Using CurseForge Maven
```gradle
repositories {
    maven {
        name = "CurseForge"
        url = "https://cursemaven.com"
    }
}

dependencies {
    modImplementation "curse.maven:voxlib-PROJECT_ID:FILE_ID"
}
```

### Using GitHub Packages
```gradle
repositories {
    maven {
        name = "GitHubPackages"
        url = "https://maven.pkg.github.com/Mystery2099/VoxLib"
        credentials {
            username = project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    modImplementation "com.github.mystery2099:voxlib:1.4.0"
}
```

## Compatibility

| | |
|---|---|
| **Minecraft** | 1.19.4 |
| **Mod Loader** | Fabric |
| **Language** | Kotlin |
| **Java** | 17+ |
| **Environment** | Client + Server |

## Links

- [GitHub Repository](https://github.com/Mystery2099/VoxLib)
- [Source Code](https://github.com/Mystery2099/VoxLib)
- [Issue Tracker](https://github.com/Mystery2099/VoxLib/issues)

## License

MMPL-1.0.1
