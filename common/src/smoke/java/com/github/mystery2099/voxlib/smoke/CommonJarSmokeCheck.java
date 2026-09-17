package com.github.mystery2099.voxlib.smoke;

import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

/** Focused boundary checks without a test framework or Minecraft client bootstrap. */
public final class CommonJarSmokeCheck {
    private static final String PACKAGE = "com.github.mystery2099.voxlib.";

    public static void main(String[] args) throws Exception {
        File commonJar = new File(args[0]);
        List<URL> classpath = new ArrayList<>();
        classpath.add(commonJar.toURI().toURL());
        for (String path : args[1].split(Pattern.quote(File.pathSeparator))) {
            classpath.add(new File(path).toURI().toURL());
        }

        // The platform parent prevents application classes from bypassing the restrictions.
        try (URLClassLoader loader = new URLClassLoader(classpath.toArray(URL[]::new), ClassLoader.getPlatformClassLoader()) {
            @Override
            protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.startsWith("net.minecraft.client.") || name.startsWith("net.fabricmc.")) {
                    throw new ClassNotFoundException("Common code tried to load a client or Fabric class: " + name);
                }
                return super.loadClass(name, resolve);
            }
        }; JarFile jar = new JarFile(commonJar)) {
            int count = 0;
            var entries = jar.entries();
            while (entries.hasMoreElements()) {
                String name = entries.nextElement().getName();
                if (!name.endsWith(".class")) continue;
                String className = name.substring(0, name.length() - ".class".length()).replace('/', '.');
                try {
                    Class<?> type = Class.forName(className, true, loader);
                    // Initialization alone need not resolve field or method signature types.
                    type.getDeclaredFields();
                    type.getDeclaredConstructors();
                    type.getDeclaredMethods();
                } catch (ReflectiveOperationException | LinkageError exception) {
                    throw new AssertionError("Common class cannot load without client or Fabric access: " + className, exception);
                }
                count++;
            }
            verifyPublicApi(loader);
            System.out.println("Verified established JVM methods and server-safe loading of " + count + " common classes.");
        }
    }

    private static void verifyPublicApi(ClassLoader loader) throws Exception {
        Class<?> shape = loader.loadClass("net.minecraft.util.shape.VoxelShape");
        Class<?> shapes = shape.arrayType();
        Class<?> function1 = loader.loadClass("kotlin.jvm.functions.Function1");
        Class<?> assembly = loader.loadClass(PACKAGE + "combination.VoxelAssembly");
        requireMethod(assembly, "createCuboidShape", shape,
            Number.class, Number.class, Number.class, Number.class, Number.class, Number.class);
        requireMethod(assembly, "and", shape, shape, shape);
        requireMethod(assembly, "plus", shape, shape, shape);
        requireMethod(assembly, "unifyWith", shape, shape, shapes);
        requireMethod(assembly, "combine", shape, loader.loadClass("net.minecraft.util.function.BooleanBiFunction"), shapes);
        requireMethod(assembly, "union", shape, shapes);
        requireMethod(assembly, "appendShapesTo", shape, shape, function1);
        requireMethod(assembly, "appendShapes", shape, shape, function1);
        requireMethod(assembly, "createSimplifiedOutlineShape", shape, shape, int.class);
        requireMethod(assembly, "createBoundingBoxShape", shape, shape);
        requireMethod(assembly, "createOutlineShape", shape,
            Number.class, Number.class, Number.class, Number.class, Number.class, Number.class, Number.class);
        requireMethod(assembly, "simplifyForOutline", shape, shape, int.class);
        requireMethod(assembly, "toBoundingBoxShape", shape, shape);

        Class<?> rotation = loader.loadClass(PACKAGE + "rotation.VoxelRotation");
        for (String name : List.of("rotateLeft", "rotateRight", "flip", "flipHorizontal", "flipVertical", "flipZ")) {
            requireMethod(rotation, name, shape, shape);
        }
        requireMethod(rotation, "rotateWithTransformation", shape, shape,
            loader.loadClass(PACKAGE + "rotation.VoxelShapeTransformation"));

        Class<?> commonShapes = loader.loadClass(PACKAGE + "shapes.CommonShapes");
        requireMethod(commonShapes, "createSlab", shape, int.class);
        requireMethod(commonShapes, "createTopSlab", shape, int.class);
        requireMethod(commonShapes, "createPillar", shape, int.class, boolean.class);
        requireMethod(commonShapes, "createTable", shape, int.class, int.class);
        requireMethod(commonShapes, "createChair", shape, int.class, boolean.class, int.class);
        requireMethod(commonShapes, "createFencePost", shape);
        requireMethod(commonShapes, "createFenceConnections", shape, boolean.class, boolean.class, boolean.class, boolean.class);
        requireMethod(commonShapes, "createStairs", shape, loader.loadClass("net.minecraft.util.math.Direction"));

        Class<?> cache = loader.loadClass(PACKAGE + "optimization.ShapeCache");
        Class<?> key = loader.loadClass(PACKAGE + "optimization.ShapeCacheKey");
        requireMethod(cache, "getOrCompute", shape, key, java.util.function.Function.class);
        requireMethod(cache, "getOrCompute", shape, key, loader.loadClass("kotlin.jvm.functions.Function0"));
        requireMethod(cache, "clearCache", void.class);
        requireMethod(cache, "invalidate", void.class, key);
        requireMethod(cache, "size", long.class);
        requireMethod(cache, "stats", String.class);

        Class<?> simplifier = loader.loadClass(PACKAGE + "optimization.ShapeSimplifier");
        requireMethod(simplifier, "simplifyToBoundingBox", shape, shape);
        requireMethod(simplifier, "simplify", shape, shape, int.class);
        requireMethod(simplifier, "createOutlineShape", shape,
            Number.class, Number.class, Number.class, Number.class, Number.class, Number.class, Number.class);
    }

    private static void requireMethod(Class<?> owner, String name, Class<?> returnType, Class<?>... parameters)
        throws ReflectiveOperationException {
        Method method = owner.getDeclaredMethod(name, parameters);
        if (!Modifier.isPublic(method.getModifiers()) || Modifier.isStatic(method.getModifiers())
            || method.getReturnType() != returnType) {
            throw new AssertionError("Established public JVM method changed: " + method);
        }
        // These entry points belong to Kotlin objects; Java callers also need INSTANCE.
        if (owner.getField("INSTANCE").get(null).getClass() != owner) {
            throw new AssertionError("Missing Kotlin object instance: " + owner.getName());
        }
    }
}
