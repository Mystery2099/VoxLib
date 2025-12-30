# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

VoxLib is a Minecraft Fabric library mod written in Kotlin that provides utilities for manipulating, creating, and rotating voxel shapes (collision/outline shapes) in Minecraft modding. It targets Minecraft 1.19.4 with Fabric API.

## Build Commands

```bash
./gradlew build                    # Build and run tests
./gradlew test                     # Run tests only
./gradlew test --tests "ShapeCacheTest"  # Run single test class
./gradlew test --tests "*cache hit*"     # Run tests matching pattern
./gradlew run                      # Run Minecraft client for testing
./gradlew publish                  # Publish to GitHub Packages (requires GITHUB_TOKEN)
./gradlew clean                    # Clean build artifacts
```

## Architecture

The codebase follows a feature-based package structure:

### Core Modules

- **`combination/`** - Shape creation and combination utilities
  - `VoxelAssembly` - Main utility object with operator overloading (`+`, `+=`, `and`) for combining shapes
  - `VoxelShapeModifier` - DSL-style builder for conditional shape assembly using `case` and `append`

- **`rotation/`** - Shape transformation utilities
  - `VoxelRotation` - Extension functions: `rotateLeft()`, `rotateRight()`, `flip()`, `flipVertical()`, `flipZ()`
  - `VoxelShapeTransformation` - Enum of transformation types

- **`optimization/`** - Performance optimization utilities
  - `ShapeCache` - Caffeine-based cache with 500-entry limit, 10-minute expiration
  - `ShapeSimplifier` - Reduces complex shapes to bounding boxes or simplified outlines

- **`shapes/`** - Pre-defined common shape generators
- **`debug/`** - In-world visualization and logging utilities

### Key Design Patterns

1. **Extension Functions** - Most operations are added as extensions to `VoxelShape` (e.g., `shape.rotateLeft()`)
2. **Operator Overloading** - `+` and `+=` for union operations, `and` for explicit combining
3. **Caching** - All transformations and unions use `ShapeCache` with `ShapeCacheKey` for automatic memoization
   - `ShapeCacheKey` is a data class: `ShapeCacheKey(originalShapeHash, operationId, parameters)`
   - Cache: 500 entries max, 10-minute access expiration, Caffeine-backed
   - Handles special cases (empty shapes, full cube) before caching to avoid unnecessary computations
4. **DSL-style Building** - `appendShapes { }` block for conditional shape assembly

### Core APIs

- **`VoxelAssembly`** - Primary API object. Import extensions directly or use the object members:
  - `VoxelAssembly.createCuboidShape(...)` - Create cuboid shapes
  - `shape1 + shape2` / `shape1.and(shape2)` - Union operations
  - `shape.appendShapes { ... }` - DSL for conditional assembly
- **`VoxelRotation`** - Extension functions: `rotateLeft()`, `rotateRight()`, `flip()`, `flipVertical()`, `flipZ()`
- **`ShapeCache.getOrCompute(key) { ... }`** - Memoization helper with Caffeine

### Dependencies

- Minecraft 1.19.4 with Fabric API
- Fabric Language Kotlin 1.13.8 (Kotlin 2.3.0)
- Caffeine 3.2.0 for caching
- JUnit 5 for testing (JUnit Jupiter)

### Package Structure

```
com.github.mystery2099.voxlib/
├── VoxLib.kt                    # Main entry point (ModInitializer)
├── combination/
│   ├── VoxelAssembly.kt         # Primary API for shape operations
│   └── VoxelShapeModifier.kt    # DSL builder for conditional assembly
├── rotation/
│   ├── VoxelRotation.kt         # Rotation/flip extensions
│   └── VoxelShapeTransformation.kt  # Transformation enum
├── optimization/
│   ├── ShapeCache.kt            # Caffeine cache with ShapeCacheKey
│   └── ShapeSimplifier.kt       # Shape simplification algorithms
├── shapes/
│   └── CommonShapes.kt          # Pre-defined shape generators
└── debug/
    └── VoxelShapeDebug.kt       # Visualization utilities
```

## Commit Guidelines

- Do not include Claude/AI references in commit messages (no "🤖 Generated with Claude Code" or "Co-Authored-By: Claude")
- Write clear, concise commit messages following conventional commit style

## Branch Strategy

For multi-file changes or features, create a feature branch:

```bash
git checkout -b feature/my-feature
# Make changes...
git push origin feature/my-feature  # Push to remote for review
# Create PR or merge when ready
```

## Release Workflow

When making changes that warrant a version bump, be proactive and handle the entire release process yourself—don't wait for explicit permission.

1. **Version bump criteria:**
   - `PATCH` (0.0.1): Bug fixes, internal refactoring, documentation updates
   - `MINOR` (0.1.0): New features, API changes, breaking changes to existing APIs
   - `MAJOR` (1.0.0): Major rewrites, significant breaking changes

2. **Update version in `gradle.properties`** (`mod_version` field)

3. **Create an annotated tag** with semantic versioning:
   ```bash
   git tag -a v{version} -m "Version {version}"
   ```

4. **Commit and push with tags:**
   ```bash
   git add .
   git commit -m "Bump version to {version}"
   git push origin {branch} --tags
   ```

5. **Tag naming convention:** `v{mod_version}` (e.g., `v1.3.0+1.19.4`)
