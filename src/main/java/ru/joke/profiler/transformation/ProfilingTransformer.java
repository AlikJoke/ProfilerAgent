package ru.joke.profiler.transformation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.output.ExecutionTimeRegistrarMetadataSelector;
import ru.joke.profiler.transformation.spy.SpyInjector;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.function.Predicate;

import static ru.joke.profiler.util.BytecodeUtil.*;

public final class ProfilingTransformer implements ClassFileTransformer {

    private static final ThreadLocal<Boolean> profilingDisabled = new ThreadLocal<>();

    private final Predicate<String> transformationFilter;
    private final StaticProfilingConfiguration configuration;
    private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;
    private final NativeClassMethodsCollector nativeClassMethodsCollector;
    private final SpyInjector spyInjector;

    public ProfilingTransformer(
            final Predicate<String> transformationFilter,
            final StaticProfilingConfiguration configuration,
            final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector,
            final NativeClassMethodsCollector nativeClassMethodsCollector,
            final SpyInjector spyInjector
    ) {
        this.transformationFilter = transformationFilter;
        this.configuration = configuration;
        this.registrarMetadataSelector = registrarMetadataSelector;
        this.nativeClassMethodsCollector = nativeClassMethodsCollector;
        this.spyInjector = spyInjector;
    }

    @Override
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classFileBuffer
    ) {
        final Boolean isProfilingDisabled = profilingDisabled.get();
        if (isProfilingDisabled != null && isProfilingDisabled
                || !this.transformationFilter.test(className)) {
            return null;
        }

        final ClassReader cr = new ClassReader(classFileBuffer);
        final ClassWriter cw = createClassWriter(cr);

        final ClassVisitor cv = new ProfilingClassTransformer(
                cw,
                className,
                this.configuration,
                this.registrarMetadataSelector,
                this.nativeClassMethodsCollector,
                this.spyInjector
        );

        cr.accept(cv, ClassReader.SKIP_FRAMES);

        return cw.toByteArray();
    }

    private static ClassWriter createClassWriter(ClassReader cr) {
        final TypeHierarchyCollector typeHierarchyCollector = new TypeHierarchyCollector();
        return new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                if (type1.equals(OBJECT_TYPE) || type2.equals(OBJECT_TYPE)) {
                    return OBJECT_TYPE;
                }

                if (type1.equals(type2)) {
                    return type1;
                }

                if (isArrayType(type1)) {
                    return isArrayType(type2)
                            ? getCommonSuperClass(getTargetArrayType(type1), getTargetArrayType(type2))
                            : OBJECT_TYPE;
                }

                if (isArrayType(type2)) {
                    return OBJECT_TYPE;
                }

                final List<String> superTypes1 = typeHierarchyCollector.collect(type1);
                final List<String> superTypes2 = typeHierarchyCollector.collect(type2);

                superTypes1.retainAll(superTypes2);
                return superTypes1.get(0);
            }
        };
    }

    public static void disable() {
        profilingDisabled.set(true);
    }

    public static void enable() {
        profilingDisabled.remove();
    }
}
