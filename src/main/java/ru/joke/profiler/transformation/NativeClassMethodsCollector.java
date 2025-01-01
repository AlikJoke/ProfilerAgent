package ru.joke.profiler.transformation;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import ru.joke.profiler.ProfilerAgent;
import ru.joke.profiler.util.BytecodeUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.objectweb.asm.ClassReader.*;
import static org.objectweb.asm.Opcodes.ASM9;

public final class NativeClassMethodsCollector extends ClassVisitor {

    private static final Logger logger = Logger.getLogger(ProfilerAgent.class.getCanonicalName());

    private final Predicate<String> filter;
    private final Map<String, Set<MethodInfo>> class2methodsMap;
    private final ThreadLocal<Set<MethodInfo>> classMethods;

    public NativeClassMethodsCollector(final Predicate<String> filter) {
        super(ASM9);
        this.class2methodsMap = new ConcurrentHashMap<>(4096, 0.5f, 256);
        this.classMethods = ThreadLocal.withInitial(HashSet::new);
        this.filter = filter;
    }

    @Override
    public MethodVisitor visitMethod(
            final int access,
            final String name,
            final String descriptor,
            final String signature,
            final String[] exceptions
    ) {
        if ((access & Opcodes.ACC_NATIVE) == 0) {
            return null;
        }

        this.classMethods.get().add(new MethodInfo(name, descriptor));
        return null;
    }

    boolean isNativeMethod(
            final String owner,
            final String methodName,
            final String methodDesc
    ) {
        final String resource = owner + "." + methodName;
        if (!this.filter.test(resource)) {
            return false;
        }

        final MethodInfo methodInfo = new MethodInfo(methodName, methodDesc);
        return this.class2methodsMap.computeIfAbsent(owner, this::collectNativeMethodsInClass).contains(methodInfo);
    }

    private Set<MethodInfo> collectNativeMethodsInClass(final String ownerClass) {

        try {
            final ClassReader classReader = new ClassReader(BytecodeUtil.toCanonicalFormat(ownerClass));
            classReader.accept(this, SKIP_CODE | SKIP_FRAMES | SKIP_DEBUG);

            return this.classMethods.get();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, String.format("Unable to collect native methods from class: %s", ownerClass), ex);
            return Collections.emptySet();
        } finally {
            this.classMethods.remove();
        }
    }

    static class MethodInfo {

        private final String name;
        private final String descriptor;

        MethodInfo(
                final String name,
                final String descriptor
        ) {
            this.name = name;
            this.descriptor = descriptor;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final MethodInfo that = (MethodInfo) o;
            return name.equals(that.name)
                    && descriptor.equals(that.descriptor);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + descriptor.hashCode();
            return result;
        }
    }
}
