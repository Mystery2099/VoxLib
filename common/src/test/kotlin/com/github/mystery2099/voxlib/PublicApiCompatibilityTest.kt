package com.github.mystery2099.voxlib

import com.github.mystery2099.voxlib.combination.VoxelAssembly
import com.github.mystery2099.voxlib.optimization.ShapeCache
import com.github.mystery2099.voxlib.optimization.ShapeCacheKey
import com.github.mystery2099.voxlib.optimization.ShapeSimplifier
import com.github.mystery2099.voxlib.rotation.VoxelRotation
import com.github.mystery2099.voxlib.rotation.VoxelShapeTransformation
import com.github.mystery2099.voxlib.shapes.CommonShapes
import kotlin.Function0
import kotlin.Function1
import net.minecraft.util.function.BooleanBiFunction
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import org.junit.jupiter.api.Test
import java.util.function.Function

class PublicApiCompatibilityTest {
    @Test
    fun `established JVM entry points remain available`() {
        requireMethods(
            VoxelAssembly::class.java,
            "createCuboidShape" to arrayOf(
                Number::class.java, Number::class.java, Number::class.java,
                Number::class.java, Number::class.java, Number::class.java
            ),
            "and" to arrayOf(VoxelShape::class.java, VoxelShape::class.java),
            "plus" to arrayOf(VoxelShape::class.java, VoxelShape::class.java),
            "unifyWith" to arrayOf(VoxelShape::class.java, Array<VoxelShape>::class.java),
            "combine" to arrayOf(BooleanBiFunction::class.java, Array<VoxelShape>::class.java),
            "union" to arrayOf(Array<VoxelShape>::class.java),
            "appendShapesTo" to arrayOf(VoxelShape::class.java, Function1::class.java),
            "appendShapes" to arrayOf(VoxelShape::class.java, Function1::class.java),
            "createSimplifiedOutlineShape" to arrayOf(VoxelShape::class.java, Int::class.javaPrimitiveType!!),
            "createBoundingBoxShape" to arrayOf(VoxelShape::class.java),
            "createOutlineShape" to arrayOf(
                Number::class.java, Number::class.java, Number::class.java,
                Number::class.java, Number::class.java, Number::class.java,
                Number::class.java
            ),
            "simplifyForOutline" to arrayOf(VoxelShape::class.java, Int::class.javaPrimitiveType!!),
            "toBoundingBoxShape" to arrayOf(VoxelShape::class.java)
        )
        requireMethods(
            VoxelRotation::class.java,
            "rotateLeft" to arrayOf(VoxelShape::class.java),
            "rotateRight" to arrayOf(VoxelShape::class.java),
            "flip" to arrayOf(VoxelShape::class.java),
            "flipHorizontal" to arrayOf(VoxelShape::class.java),
            "flipVertical" to arrayOf(VoxelShape::class.java),
            "flipZ" to arrayOf(VoxelShape::class.java),
            "rotateWithTransformation" to arrayOf(
                VoxelShape::class.java,
                VoxelShapeTransformation::class.java
            )
        )
        requireMethods(
            CommonShapes::class.java,
            "createSlab" to arrayOf(Int::class.javaPrimitiveType!!),
            "createTopSlab" to arrayOf(Int::class.javaPrimitiveType!!),
            "createPillar" to arrayOf(Int::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!),
            "createTable" to arrayOf(Int::class.javaPrimitiveType!!, Int::class.javaPrimitiveType!!),
            "createChair" to arrayOf(
                Int::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                Int::class.javaPrimitiveType!!
            ),
            "createFencePost" to emptyArray(),
            "createFenceConnections" to arrayOf(
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!,
                Boolean::class.javaPrimitiveType!!
            ),
            "createStairs" to arrayOf(Direction::class.java)
        )
        requireMethods(
            ShapeCache::class.java,
            "getOrCompute" to arrayOf(ShapeCacheKey::class.java, Function::class.java),
            "getOrCompute" to arrayOf(ShapeCacheKey::class.java, Function0::class.java),
            "clearCache" to emptyArray(),
            "invalidate" to arrayOf(ShapeCacheKey::class.java),
            "size" to emptyArray(),
            "stats" to emptyArray()
        )
        requireMethods(
            ShapeSimplifier::class.java,
            "simplifyToBoundingBox" to arrayOf(VoxelShape::class.java),
            "simplify" to arrayOf(VoxelShape::class.java, Int::class.javaPrimitiveType!!),
            "createOutlineShape" to arrayOf(
                Number::class.java, Number::class.java, Number::class.java,
                Number::class.java, Number::class.java, Number::class.java,
                Number::class.java
            )
        )
    }

    private fun requireMethods(
        owner: Class<*>,
        vararg methods: Pair<String, Array<Class<*>>>
    ) {
        methods.forEach { (name, parameterTypes) ->
            owner.getMethod(name, *parameterTypes)
        }
    }
}
