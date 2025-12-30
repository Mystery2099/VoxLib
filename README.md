# VoxLib

A Minecraft Fabric library mod that provides utilities for manipulating, creating, and rotating voxel shapes in your code!

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.19.4-green)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![Language](https://img.shields.io/badge/Language-Kotlin-purple)

## Features

- **Create** voxel shapes with intuitive syntax
- **Combine** shapes using operator overloading (`+`) and conditional assembly
- **Transform** shapes with rotation and flipping utilities
- **Simplify** your block collision and outline code
- **Optimize** performance with high-performance caching (using Caffeine) and shape simplification utilities
- **Debug** shapes with visualization tools

## Getting Started

### Installation

There are several ways to add VoxLib as a dependency to your project:

#### Option 1: CurseForge Maven (Recommended for Minecraft Mods)

Add VoxLib as a dependency using CurseForge Maven in your `build.gradle` file:

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

Replace:
- `PROJECT_ID` with the CurseForge project ID for VoxLib
- `FILE_ID` with the file ID of the specific version you want to use

You can find these IDs on the VoxLib CurseForge page under the "Files" tab.

#### Option 2: JitPack (Easiest Setup)

JitPack makes it easy to use any GitHub repository as a dependency:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation 'com.github.Mystery2099:VoxLib:TAG'
}
```

Replace `TAG` with a version tag like `v1.4.0` or use `master-SNAPSHOT` for the latest development version.

#### Option 3: GitHub Packages

For projects that use GitHub Packages:

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
    modImplementation "com.github.mystery2099:voxlib:VERSION"
}
```

You'll need to:
1. Set `gpr.user` and `gpr.key` in your `~/.gradle/gradle.properties` file, or
2. Set `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables

For more information on GitHub Packages, see [Working with a GitHub Packages Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)

## Usage Examples

### Creating Shapes

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape

// Create a simple cuboid shape (parameters: minX, minY, minZ, maxX, maxY, maxZ)
val baseShape = createCuboidShape(0, 0, 0, 16, 1, 16) // A slab at the bottom of the block
```

### Pre-defined Common Shapes

```kotlin
import com.github.mystery2099.voxlib.shapes.CommonShapes

// Common pre-defined shapes for quick use
val slab = CommonShapes.createSlab(8)           // Half-height slab
val slabTop = CommonShapes.createTopSlab(8)     // Top half slab
val pillar = CommonShapes.createPillar(6)       // 6-wide centered pillar
val table = CommonShapes.createTable()          // Default table
val chair = CommonShapes.createChair()          // Default chair with backrest
val stairsShape = CommonShapes.createStairs(Direction.NORTH) // Stairs facing north
val fencePost = CommonShapes.createFencePost()  // Standard fence post

// Fence with connections
val fenceWithSides = CommonShapes.createFenceConnections(
    north = true, east = true, south = false, west = false
)
```

### Combining Shapes

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape

// Create individual shapes
val base = createCuboidShape(0, 0, 0, 16, 1, 16)  // Bottom slab
val post = createCuboidShape(7, 1, 7, 9, 15, 9)   // Center post
val top = createCuboidShape(6, 15, 6, 10, 16, 10) // Top piece

// Combine them using the + operator
val tableShape = base + post + top
```

### Conditional Shape Assembly

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.appendShapes
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape

fun createChairShape(hasBackrest: Boolean): VoxelShape {
    val seat = createCuboidShape(2, 8, 2, 14, 10, 14)  // Seat
    val legs = createCuboidShape(3, 0, 3, 13, 8, 13)   // Legs

    return seat appendShapes {
        // Only add backrest if the condition is true
        createCuboidShape(3, 10, 12, 13, 20, 14) case hasBackrest

        // Always add legs
        append(legs)
    }
}
```

### Rotating Shapes

```kotlin
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateLeft
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateRight
import com.github.mystery2099.voxlib.rotation.VoxelRotation.flip

// Create a shape for a directional block
val northFacingShape = createCuboidShape(5, 0, 0, 11, 16, 8)

// Rotate for different directions
val eastFacingShape = northFacingShape.rotateRight()
val southFacingShape = northFacingShape.flip()
val westFacingShape = northFacingShape.rotateLeft()
```

### Performance Optimization

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createSimplifiedOutlineShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createOutlineShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.simplifyForOutline

// Create a complex collision shape
val complexCollisionShape = createComplexShape()

// Create a simplified outline shape for better performance
val outlineShape = complexCollisionShape.simplifyForOutline(maxBoxes = 8)

// Or create a bounding box shape (even simpler)
val boundingBoxShape = complexCollisionShape.toBoundingBoxShape()

// Create an efficient hollow outline shape directly
val efficientOutline = createOutlineShape(
    minX = 0, minY = 0, minZ = 0,
    maxX = 16, maxY = 16, maxZ = 16,
    thickness = 1
)

// All transformations use Caffeine caching by default for better performance
val rotatedShape = complexShape.rotateRight() // Uses cache automatically

// Caffeine provides automatic cache eviction and time-based expiration
// so you don't need to worry about memory leaks

// You can also use the VoxelAssembly utilities for optimized shape operations
val optimizedUnion = VoxelAssembly.union(shape1, shape2, shape3) // Optimized union operation
```

### Debug Tools

VoxLib provides debugging utilities to help you visualize and diagnose voxel shapes during development.

**Note:** Some debug tools are client-only. The `@Environment(EnvType.CLIENT)` annotation ensures they only load on the client, preventing server crashes.

#### Client-Side Rendering

```kotlin
import com.github.mystery2099.voxlib.debug.VoxelShapeDebug
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
import net.minecraft.util.math.BlockPos
import java.awt.Color

// Create a shape to debug
val shape = createCuboidShape(0, 0, 0, 16, 8, 16)

// Render it at a block position (client-side only)
VoxelShapeDebug.renderShape(
    matrices,
    vertexConsumers,
    shape,
    BlockPos(x, y, z),
    color = Color.GREEN,
    alpha = 0.5f
)
```

#### Server-Side Logging

For debugging on servers or in headless environments:

```kotlin
import com.github.mystery2099.voxlib.debug.VoxelShapeDebug

// Log shape information to console
VoxelShapeDebug.logShapeInfo(shape, "MyShape")

// Compare two shapes
VoxelShapeDebug.compareShapes(shape1, shape2, "Original", "Rotated")
```

These logging functions work on both client and server environments.

## Documentation

For full documentation of all available utilities, see the KDoc comments in the source code or visit the [GitHub repository](https://github.com/Mystery2099/VoxLib).