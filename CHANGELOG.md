# Changelog

All notable changes to VoxLib will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Added a manual JMH benchmark suite with vanilla and legacy baselines
- Added exact occupied-volume and JVM API compatibility tests

### Changed

- Replaced list-based operation cache keys with specialized identity keys
- Kept first-use binary union overhead close to vanilla with two-touch cache admission
- Made repeated transformation cache hits effectively allocation-free and clear calling-thread fast-path state immediately
- Removed per-box coordinate arrays from shape transformations
- Memoized finite `CommonShapes` parameter combinations and canonicalized chairs without backrests, reducing the maximum slot count from 501 to 321
- Reduced 256-box simplifier allocation from about 10.93 MB/op to 1.88 MB/op with a compact deterministic queue
- Centralized Minecraft 1.19.4 union mechanics for easier future ports

## [1.6.0] - 2026-07-30

### Added

- Added independent targeted block outline and collision shape overlays

### Changed

- Added ModMenu to local Fabric runtime testing

### Fixed

- Preserved configuration defaults when loading partial JSON
- Serialized configuration updates and file writes
- Kept debug shape transparency independent from configured RGB color

## [1.5.0] - 2025-12-30

### Added

- Debug tools infrastructure with client-side support
- `VoxLibConfig` - Validated, persisted client debug settings
- `VoxelShapeDebugClient` - Client-only debug state management
- Client environment guards for `VoxelShapeDebug` rendering methods
- `renderShapeWithConfig()` helper method using client config settings
- Native config screen with debug mode, color, alpha, and reset controls

### Changed

- Updated version to 1.5.0+1.19.4
- Added client entrypoint `VoxLibClient` for debug features
- Added optional ModMenu integration for config UI access

### Fixed

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

[1.5.0]: https://github.com/Mystery2099/VoxLib/compare/v1.4.0%2B1.19.4...v1.5.0%2B1.19.4
[1.4.0]: https://github.com/Mystery2099/VoxLib/compare/v1.2.0...v1.4.0%2B1.19.4
[1.2.0]: https://github.com/Mystery2099/VoxLib/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/Mystery2099/VoxLib/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/Mystery2099/VoxLib/releases/tag/v1.0.0%2B1.19.4
