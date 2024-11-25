package ru.joke.profiler.core;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;

final class ProfilingTransformer implements ClassFileTransformer {

    private final ProfilingConfiguration configuration;

    ProfilingTransformer(final ProfilingConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public byte[] transform(
            ClassLoader loader,
            String className,
            Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain,
            byte[] classFileBuffer) {

        final Set<String> targetPackages = this.configuration.getTargetPackages();
        if (isClassFromSystemPackage(className)
                || isClassFromAgentLibraryPackage(className)) {
            return null;
        }
        // TODO check class name in scanned packages from config

        final ClassReader cr = new ClassReader(classFileBuffer);
        final ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        final ClassVisitor cv = new ExecutionTimeProfilingVisitor(cw, className);
        cr.accept(cv, ClassReader.EXPAND_FRAMES);

        return cw.toByteArray();
    }

    private boolean isClassFromSystemPackage(final String className) {
        return className.startsWith("java/")
                || className.startsWith("jdk/")
                || className.startsWith("javax/")
                || className.startsWith("sun/")
                || className.startsWith("com/sun/")
                || className.startsWith("org/objectweb/asm/");
    }

    private boolean isClassFromAgentLibraryPackage(final String className) {
        return className.startsWith("ru/joke/profiler/");
    }

    static class ExecutionTimeProfilingVisitor extends ClassVisitor {

        private final String className;

        ExecutionTimeProfilingVisitor(final ClassWriter classWriter, final String className) {
            super(Opcodes.ASM9, classWriter);
            this.className = className;
        }

        @Override
        public MethodVisitor visitMethod(int methodAccess, String methodName, String methodDesc, String signature, String[] exceptions) {
            final MethodVisitor methodVisitor = this.cv.visitMethod(methodAccess, methodName, methodDesc, signature, exceptions);
            final String fullMethodName = this.className + "." + methodName;
            return new TimestampLocalVarInjector(Opcodes.ASM9, methodAccess, methodDesc, methodVisitor, fullMethodName);
        }

        private static class TimestampLocalVarInjector extends LocalVariablesSorter {

            private final String methodName;
            private int timestampEnterVarIndex;

            protected TimestampLocalVarInjector(int api, int access, String descriptor, MethodVisitor methodVisitor, String methodName) {
                super(api, access, descriptor, methodVisitor);
                this.methodName = methodName;
            }

            @Override
            public void visitCode() {
                this.timestampEnterVarIndex = newLocal(Type.LONG_TYPE);

                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                mv.visitVarInsn(LSTORE, this.timestampEnterVarIndex);

                super.visitCode();
            }

            @Override
            public void visitInsn(int opcode) {
                if (opcode >= IRETURN && opcode <= RETURN) {
                    final int timestampExitVarIndex = newLocal(Type.LONG_TYPE);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
                    mv.visitVarInsn(LSTORE, timestampExitVarIndex);

                    mv.visitLdcInsn(this.methodName);

                    mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
                    mv.visitVarInsn(LLOAD, timestampExitVarIndex);

                    mv.visitMethodInsn(INVOKESTATIC, "ru/joke/profiler/core/ExecutionTimeRegistrar", "register", "(Ljava/lang/String;JJ)V", false);
                }

                super.visitInsn(opcode);
            }
        }
    }
}
