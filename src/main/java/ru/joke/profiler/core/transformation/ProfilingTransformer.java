package ru.joke.profiler.core.transformation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.output.ExecutionTimeRegistrarMetadataSelector;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.function.Predicate;

public final class ProfilingTransformer implements ClassFileTransformer {

    private final Predicate<String> transformationFilter;
    private final StaticProfilingConfiguration configuration;
    private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;

    public ProfilingTransformer(
            final Predicate<String> transformationFilter,
            final StaticProfilingConfiguration configuration,
            final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector) {
        this.transformationFilter = transformationFilter;
        this.configuration = configuration;
        this.registrarMetadataSelector = registrarMetadataSelector;
    }

    @Override
    public byte[] transform(
            final ClassLoader loader,
            final String className,
            final Class<?> classBeingRedefined,
            final ProtectionDomain protectionDomain,
            final byte[] classFileBuffer) {

        if (!this.transformationFilter.test(className)) {
            return null;
        }

        final ClassReader cr = new ClassReader(classFileBuffer);
        final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ClassVisitor cv = new ExecutionTimeProfilingVisitor(cw, className, this.configuration, this.registrarMetadataSelector);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }
}
