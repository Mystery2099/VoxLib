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
- **Debug** shapes with targeted outline and collision overlays

## Getting Started

### Installation

VoxLib is published through GitHub Packages. Add the repository and dependency to your `build.gradle`:

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

Replace `VERSION` with the VoxLib version you want to use. GitHub Packages requires authentication, even when downloading a public package. You can either:

1. Set `gpr.user` and `gpr.key` in your Gradle user properties (`~/.gradle/gradle.properties`).
2. Set the `GITHUB_ACTOR` and `GITHUB_TOKEN` environment variables.

The token needs the `read:packages` scope. Avoid committing it to your project.

For more information on GitHub Packages, see [Working with a GitHub Packages Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package)

VoxLib also requires Fabric API and Fabric Language Kotlin at runtime. Use versions compatible with Minecraft 1.19.4.

### Building from Source

If you would rather use a local build:

```shell
./gradlew build
```

The finished mod JAR will be written to `build/libs`. You can also run `./gradlew publishToMavenLocal` and use `mavenLocal()` while developing another mod.

## Usage Examples

### Debugging Targeted Blocks

With Mod Menu installed, open the VoxLib settings and enable **Debug Mode**. You can then show the outline shape, collision shape, or both for the block under your crosshair. The overlay uses the color and transparency configured on the same screen.

### Creating Shapes

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape

// Create a simple cuboid shape (parameters: minX, minY, minZ, maxX, maxY, maxZ)
val baseShape = createCuboidShape(0, 0, 0, 16, 1, 16) // A slab at the bottom of the block
```

### Pre-defined Common Shapes

```kotlin
import com.github.mystery2099.voxlib.shapes.CommonShapes
import net.minecraft.util.math.Direction

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
import com.github.mystery2099.voxlib.combination.VoxelAssembly.plus

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
import net.minecraft.util.shape.VoxelShape

fun createChairShape(hasBackrest: Boolean): VoxelShape {
    val seat = createCuboidShape(2, 8, 2, 14, 10, 14)  // Seat
    val legs = createCuboidShape(3, 0, 3, 13, 8, 13)   // Legs

    return seat appendShapes {
        // Only add backrest if the condition is true
        createCuboidShape(3, 10, 12, 13, 16, 14) case hasBackrest

        // Always add legs
        append(legs)
    }
}
```

### Rotating Shapes

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createCuboidShape
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
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createOutlineShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.simplifyForOutline
import com.github.mystery2099.voxlib.combination.VoxelAssembly.toBoundingBoxShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.union
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateRight
import com.github.mystery2099.voxlib.shapes.CommonShapes

val complexShape = CommonShapes.createTable()

// Reduce detail for an outline. Keep the original shape for collision checks.
val outlineShape = complexShape.simplifyForOutline(maxBoxes = 8)

// Or use one bounding box when an exact outline is not important.
val boundingBoxShape = complexShape.toBoundingBoxShape()

// Create a hollow outline directly.
val efficientOutline = createOutlineShape(
    minX = 0, minY = 0, minZ = 0,
    maxX = 16, maxY = 16, maxZ = 16,
    thickness = 1
)

// Rotations and small union operations are cached automatically.
val rotatedShape = complexShape.rotateRight() // Uses cache automatically
val combinedShape = union(complexShape, efficientOutline)
```

## Documentation

For full documentation of all available utilities, see the KDoc comments in the source code or visit the [GitHub repository](https://github.com/Mystery2099/VoxLib).

## Compatibility

- Minecraft 1.19.4
- Fabric Loader 0.18.4 or newer
- Java 17 or newer
- Client and dedicated server
- Mod Menu is optional and only needed for the in-game debug settings screen

## License

VoxLib is available under the [Minecraft Mod Public License 1.0.1](LICENSE).
