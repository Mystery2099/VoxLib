# VoxLib

VoxLib is a lightweight **Fabric and NeoForge library for working with Minecraft's `VoxelShape` system**.

It originally started as a collection of utilities inside my **Wooden Accents Mod**. I kept finding myself writing the same kinds of shape code over and over again, and since I was already using Kotlin, I started turning that code into more intuitive and readable helpers instead of constantly dealing with the raw `VoxelShape` API.

Eventually, that utility code got big enough that keeping it buried inside Wooden Accents stopped making much sense. I split it into its own project so I could reuse it across other mods without copying everything around every time.

VoxLib provides utilities for creating, combining, rotating, flipping, simplifying, caching, and debugging voxel shapes while still using Minecraft's normal `VoxelShape` system underneath.

## For Players

If you're here because another mod says VoxLib is required, you probably don't need to know much about the API side of things.

Just install the correct VoxLib version for your Minecraft version and mod loader, put it in your mods folder, and you're good.

## What does it actually do?

VoxLib mainly gives mod developers a cleaner way to work with voxel shapes.

Some of the stuff included:

* Easier creation of cuboid shapes using Minecraft's normal 0–16 block coordinates
* Shape combination using operators like `+`
* Left/right rotation and flipping utilities
* Conditional shape assembly
* Reusable common shapes for things like slabs, pillars, tables, chairs, stairs, and fences
* Bounded caching for repeated rotations and shape unions
* Optional shape simplification tools for less-detailed outlines
* Debug tools for visualizing outline and collision shapes in-game
* Shape information and comparison utilities for debugging

For example, instead of manually combining everything:

```kotlin
val tableShape = base + post + top
```

Or, if you've already made the north-facing version of a block:

```kotlin
val east = north.rotateRight()
val south = north.flip()
val west = north.rotateLeft()
```

That's pretty much the idea behind VoxLib: **make the annoying parts of working with voxel shapes easier without trying to completely replace how Minecraft handles them.**

## Common Shapes

VoxLib also includes factories for a few shapes that tend to get recreated constantly:

```kotlin
val slab = CommonShapes.createSlab(8)
val pillar = CommonShapes.createPillar(6)
val table = CommonShapes.createTable()
val chair = CommonShapes.createChair()
val stairs = CommonShapes.createStairs(Direction.NORTH)
val fencePost = CommonShapes.createFencePost()
```

These are mostly there as convenient starting points. You obviously don't have to build everything around them.

## Debug Tools

One of the more useful parts of VoxLib during development is its shape debugging system.

Debug mode can visualize the block currently under your crosshair and independently display its:

* Outline shape
* Collision shape
* Or both at the same time

The overlay color and transparency can also be configured.

On Fabric, the settings screen can be accessed through **Mod Menu** if it is installed. On NeoForge, it is available through the Mods screen.

The rendering side of the debug system is entirely client-side and does not load on dedicated servers.

## Performance

VoxLib is primarily a **convenience library**, not some magical faster replacement for Minecraft's voxel shape implementation.

Most operations still eventually use Minecraft's own `VoxelShape` APIs. If a shape never changes, you're normally better off creating it once and storing it as a constant rather than rebuilding it every time Minecraft asks for the block's shape.

After VoxLib became its own project, I also added **Caffeine** to handle bounded caching for repeated transformations and unions. The goal there is to hopefully cut down on repeated work in cases where the same operations are happening over and over again, but I'm intentionally not pretending that slapping a cache on something automatically makes it faster.

VoxLib also memoizes its common-shape factories and includes optional simplification utilities, but those are meant for cases where they actually make sense.

Basically: use the convenient APIs, but don't throw basic performance practices out the window just because there's a cache involved.

## Compatibility

VoxLib currently supports:

* **Minecraft 1.21.1**
* **Fabric**
* **NeoForge**
* **Java 21+**
* Both client and dedicated servers

Fabric requires **Fabric API** and **Fabric Language Kotlin**.

The NeoForge build bundles the Kotlin standard library and Caffeine, so an additional Kotlin dependency is not required there.

### Older Minecraft Versions

The **1.19.x and 1.20.x branches are now in bug-fix-only mode**.

They may still receive fixes for actual bugs or serious issues, but new features and larger improvements will be focused on the current version of VoxLib instead.

I don't really want to maintain multiple actively-developed versions of the same library forever, so older Minecraft versions will stay available without holding back development of the current one.

## Using VoxLib in Your Mod

### Fabric

Fabric developers can use VoxLib directly through Modrinth's Maven repository:

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

Replace `VERSION` with the VoxLib version you want to use.

### NeoForge

NeoForge uses a separate artifact, so don't use the Fabric coordinate above.

For NeoForge setup, dependency information, and the rest of the installation details, check the **[VoxLib installation guide](https://github.com/Mystery2099/VoxLib/wiki/Installation)**.

The wiki also contains more detailed setup information, API examples, and documentation.

## AI Usage

VoxLib was originally created and developed by me without AI assistance, and the core ideas and direction of the project are still my own.

More recent development has included AI-assisted coding, particularly while overhauling, refactoring, testing, documenting, and porting parts of the existing project. I still review and make the decisions around what actually goes into VoxLib; AI is mainly another development tool I use to speed up work that I would otherwise be doing manually.

## Source & Issues

VoxLib is open source, so if you want to dig through the code, report something broken, or see how any of it works:

* [**GitHub Repository**](https://github.com/Mystery2099/VoxLib)
* [**Issue Tracker**](https://github.com/Mystery2099/VoxLib/issues)

## Support

VoxLib is free, and it always will be. Nobody owes me anything for it.

If you somehow still want to support my projects, you can [buy me a coffee](https://buymeacoffee.com/mystery2099). No pressure at all.

## License

VoxLib is licensed under the **Minecraft Mod Public License 1.0.1 (MMPL-1.0.1)**.
