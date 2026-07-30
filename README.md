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
- **Reuse** repeated transformations with bounded caching and optionally create approximate, lower-detail outline shapes
- **Debug** shapes with targeted outline and collision overlays

## Getting Started

### Installation

#### Modrinth Maven (Recommended)

Every VoxLib release uploaded to Modrinth is automatically available through its Maven repository. Add the following to your `build.gradle`:

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

Replace `VERSION` with a version listed on the [VoxLib Modrinth page](https://modrinth.com/mod/voxlib/versions), such as `1.4.0+1.19.4`. Modrinth does not require a username or access token.

#### GitHub Packages

GitHub Packages is also available if you prefer to use it:

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

Replace `VERSION` with the VoxLib version you want to use. GitHub Packages requires authentication, even for public packages. You can either:

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

### Performance Considerations

VoxLib is primarily a convenience library. Its creation and combination APIs
ultimately use Minecraft's built-in `VoxelShape` operations, so using VoxLib
does not automatically make shape handling faster than vanilla.

For fixed block shapes, prefer constructing the shape once and storing it in a
static field or Kotlin companion object:

```kotlin
companion object {
    private val TABLE_SHAPE = CommonShapes.createTable()
}
```

This avoids both repeated construction and cache lookup overhead. VoxLib's
bounded cache is most useful when the same transformation or binary union is
repeated with the same `VoxelShape` instances. Binary unions use two-touch
admission: the first call stays close to vanilla, the second admits the pair,
and later same-thread calls can reuse the result. Calls made with newly-created
shape instances should not be expected to hit the cache.

`CommonShapes` factories memoize their finite parameter combinations. A field
or companion-object constant is still preferable for fixed block geometry
because it avoids validation and lookup on every shape query.

The simplification utilities below may reduce the cost of later outline
queries, but they deliberately approximate the original geometry by filling
some empty space. Do not use a simplified shape where exact collision,
raycasting, or outline geometry is required.

```kotlin
import com.github.mystery2099.voxlib.combination.VoxelAssembly.createOutlineShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.simplifyForOutline
import com.github.mystery2099.voxlib.combination.VoxelAssembly.toBoundingBoxShape
import com.github.mystery2099.voxlib.combination.VoxelAssembly.union
import com.github.mystery2099.voxlib.rotation.VoxelRotation.rotateRight
import com.github.mystery2099.voxlib.shapes.CommonShapes

val complexShape = CommonShapes.createTable()

// Approximate the outline with fewer boxes.
// Keep the original shape for collision checks and exact outlines.
val outlineShape = complexShape.simplifyForOutline(maxBoxes = 8)

// Use one bounding box only when an approximate outline is acceptable.
val boundingBoxShape = complexShape.toBoundingBoxShape()

// Create a hollow shape directly. This is still composed with vanilla
// VoxelShape operations and is not inherently faster than a solid cuboid.
val hollowShape = createOutlineShape(
    minX = 0, minY = 0, minZ = 0,
    maxX = 16, maxY = 16, maxZ = 16,
    thickness = 1
)

// Repeating this operation with the same complexShape instance can use the cache.
val rotatedShape = complexShape.rotateRight()
val combinedShape = union(complexShape, hollowShape)
```

The project includes a manual JMH suite and performance gates. See
[Performance benchmarks](docs/PERFORMANCE.md) for its methodology, scoped
results, and commands. Measure with your actual shapes and call patterns before
choosing VoxLib APIs for performance-sensitive paths.

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

## Compatibility

- Minecraft 1.19.4
- Fabric Loader 0.18.4 or newer
- Java 17 or newer
- Client and dedicated server
- Mod Menu is optional and only needed for the in-game debug settings screen

## License

VoxLib is available under the [Minecraft Mod Public License 1.0.1](LICENSE).
