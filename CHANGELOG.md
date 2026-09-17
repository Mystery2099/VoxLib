# Changelog

All notable changes to VoxLib will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Nothing yet.

### Changed

- Reused vanilla bounding-box helpers and removed temporary boxes from debug rendering
- Deprecated the misleading `and` union alias in favor of `+`, preserving its existing behavior and JVM method
- Fixed unions with full cubes discarding geometry outside the block bounds
- Converted the NeoForge entrypoints from Java to Kotlin

- Updated Fabric Loader to 0.19.5, Fabric Language Kotlin to 1.14.1, and Kotlin to 2.4.20
- Updated Loom to 1.18.2 and the build daemon to Java 25; the mod still targets Java 21
- Updated CI checkout, Java setup, and artifact upload actions to their latest release lines

- Ported both Fabric and NeoForge builds to Minecraft 1.21.1
- Updated Fabric API to 0.116.17, NeoForge to 21.1.250, and Mod Menu to 11.0.4
- Updated loader compatibility metadata and the resource pack format for 1.21.1
- Adapted NeoForge config-screen registration to its 1.21.1 API

## [1.7.0] - 2026-09-17

### Added

- Added a NeoForge build with bundled Kotlin and Caffeine
- Added NeoForge config-screen and targeted shape-highlight integration

### Changed

- Ported the mod to Minecraft 1.20.6, raising the mod to Java 21 and Fabric API 0.99.4
- Replaced the Forge module with a NeoForge module published as `voxlib-neoforge` via ModDevGradle
- Updated Mod Menu to 10.0.0 and raised the Java requirement to 21
- Updated the wrapper to Gradle 9.7.1 and the build daemon to Java 21 while retaining Java 17 mod compatibility
- Replaced ForgeGradle with ModDevGradle's legacy Forge plugin for Gradle 9 support

## [1.6.1+1.20.1] - 2026-09-17

### Added

- Added a Forge 1.20.1 build with bundled Kotlin and Caffeine
- Added Forge config-screen and targeted shape-highlight integration

### Changed

- Shared client configuration and debug rendering between Fabric and Forge
- Switched development sources to Mojang mappings for both loaders
- Added loader suffixes to distributable filenames; kept Fabric Maven coordinates and added `voxlib-forge`

- Ported the Fabric mod and common library to Minecraft 1.20.1
- Updated Yarn mappings, Fabric API, and Mod Menu for 1.20.1
- Migrated the settings screen to Minecraft's DrawContext rendering API

## [1.6.1] - 2026-08-01

### Added

- Added a manual JMH benchmark suite with vanilla and legacy baselines
- Added exact occupied-volume and JVM API compatibility tests

### Changed

- Replaced list-based operation cache keys with specialized identity keys
- Kept first-use binary union overhead close to vanilla with two-touch admission and a small recent-pair ring
- Made repeated transformation cache hits effectively allocation-free and clear calling-thread fast-path state immediately
- Removed per-box coordinate arrays from shape transformations
- Memoized finite `CommonShapes` parameter combinations and canonicalized chairs without backrests, reducing the maximum slot count from 501 to 321
- Reduced 256-box simplifier allocation from about 10.93 MB/op to 1.88 MB/op with a compact deterministic queue
- Centralized Minecraft 1.19.4 union mechanics for easier future ports

## [1.6.0] - 2026-07-30

### Added

- Debug tools infrastructure with client-side support
- `VoxLibConfig` - Validated, persisted client debug settings
- `VoxelShapeDebugClient` - Client-only debug state management
- Client environment guards for `VoxelShapeDebug` rendering methods
- `renderShapeWithConfig()` helper method using client config settings
- Native config screen with debug mode, color, alpha, and reset controls
- Client entrypoint `VoxLibClient` for debug features
- Optional ModMenu integration for config UI access
- Added ModMenu to local Fabric runtime testing
- Independent targeted block outline and collision shape overlays

### Changed

- Updated version to 1.6.0+1.19.4

### Fixed

- Preserved configuration defaults when loading partial JSON
- Serialized configuration updates and file writes
- Kept debug shape transparency independent from configured RGB color
- Added client-side environment guards to prevent server crashes
- Prevented cache hash collisions from returning unrelated shapes
- Handled empty bounding-box simplification and invalid box limits safely
- Replaced placeholder tests with explicit skips and real asymmetric rotation coverage

## [1.4.0] - 2025-12-30

### Added

- Comprehensive unit tests for VoxLib APIs
- `fabric.mod.json` for test environment

### Changed

- Updated dependencies for Minecraft 1.19.4
- Updated Fabric Language Kotlin to 1.13.8+kotlin.2.3.0

## [1.3.0] - 2025-12-30

### Changed

- Fixed Gradle deprecation warnings
- Applied Kotlin best practices
- Improved type safety in VoxLib APIs
- Enhanced documentation

## [1.2.0] - 2023-05-15

### Added

- New transformation methods:
    - `flipVertical()` - Flips a VoxelShape vertically (around X axis)
    - `flipZ()` - Flips a VoxelShape along the Z axis
    - `rotate(transformation)` - General-purpose method for any transformation
    - Renamed `flip()` to `flipHorizontal()` with backward compatibility
- Performance optimizations:
    - High-performance caching using Caffeine library
    - Optimized shape combination algorithms
    - Special case handling for empty and full shapes
    - Automatic cache eviction with time-based expiration
- New utility class `CommonShapes` with pre-defined shape generators:
    - Slabs (bottom and top)
    - Pillars
    - Tables
    - Chairs
    - Fence posts and connections
    - Stairs
- New shape simplification utilities:
    - `createSimplifiedOutlineShape()` - Reduces complex shapes to fewer boxes
    - `createBoundingBoxShape()` - Creates a simple bounding box from a complex shape
    - `createOutlineShape()` - Creates efficient hollow box shapes for outlines
    - Extension methods: `simplifyForOutline()` and `toBoundingBoxShape()`
- New debug utilities in `VoxelShapeDebug`:
    - Shape rendering in-world
    - Shape information logging
    - Shape comparison tools
- Enhanced documentation:
    - Improved README with examples
    - Added badges
    - More detailed KDoc comments
    - Added CurseForge Maven dependency instructions

### Changed

- Renamed `flip()` to `flipHorizontal()` (with backward compatibility)
- Improved documentation for all existing methods
- Added version constants to main VoxLib class
- Improved union operation with divide-and-conquer algorithm
- Optimized shape transformation methods
- Added early returns for special cases to improve performance
- Updated Gradle to 8.12
- Updated Fabric Loader to 0.16.13
- Updated Fabric Language Kotlin to 1.13.2+kotlin.2.1.20
- Specified minimum Fabric API version as 0.87.2+1.19.4

## [1.1.0] - 2021-06-10

### Added

- Initial release with basic shape manipulation utilities
- VoxelAssembly for creating and combining shapes
- VoxelRotation for transforming shapes
- Basic GitHub Actions setup

## [1.0.0] - 2021-05-25

### Added

- Initial project setup
- Basic mod structure

[Unreleased]: https://github.com/Mystery2099/VoxLib/compare/v1.7.0%2B1.20.6...HEAD
[1.7.0]: https://github.com/Mystery2099/VoxLib/compare/v1.6.1%2B1.20.1...v1.7.0%2B1.20.6
[1.6.1+1.20.1]: https://github.com/Mystery2099/VoxLib/compare/v1.6.1%2B1.19.4...v1.6.1%2B1.20.1
[1.6.1]: https://github.com/Mystery2099/VoxLib/compare/v1.6.0%2B1.19.4...v1.6.1%2B1.19.4
[1.6.0]: https://github.com/Mystery2099/VoxLib/compare/v1.4.0%2B1.19.4...v1.6.0%2B1.19.4
[1.4.0]: https://github.com/Mystery2099/VoxLib/compare/v1.2.0...v1.4.0%2B1.19.4
[1.2.0]: https://github.com/Mystery2099/VoxLib/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Mystery2099/VoxLib/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Mystery2099/VoxLib/releases/tag/v1.0.0%2B1.19.4
