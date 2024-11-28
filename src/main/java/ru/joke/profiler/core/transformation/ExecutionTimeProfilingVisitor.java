package ru.joke.profiler.core.transformation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;

import static org.objectweb.asm.Opcodes.*;

final class ExecutionTimeProfilingVisitor extends ClassVisitor {

    private final String className;
    private final StaticProfilingConfiguration profilingConfiguration;

    ExecutionTimeProfilingVisitor(
            final ClassWriter classWriter,
            final String className,
            final StaticProfilingConfiguration profilingConfiguration) {
        super(Opcodes.ASM9, classWriter);
        this.className = className;
        this.profilingConfiguration = profilingConfiguration;
    }

    @Override
    public MethodVisitor visitMethod(
            final int methodAccess,
            final String methodName,
            final String methodDesc,
            final String signature,
            final String[] exceptions) {
        final MethodVisitor methodVisitor = this.cv.visitMethod(methodAccess, methodName, methodDesc, signature, exceptions);
        final String fullMethodName = this.className + "." + methodName;
        return new TimestampLocalVarInjector(Opcodes.ASM9, methodAccess, methodDesc, methodVisitor, fullMethodName, profilingConfiguration);
    }

    private static class TimestampLocalVarInjector extends LocalVariablesSorter {

        private final StaticProfilingConfiguration profilingConfiguration;
        private final String methodName;
        private int timestampEnterVarIndex;

        TimestampLocalVarInjector(
                final int api,
                final int access,
                final String descriptor,
                final MethodVisitor methodVisitor,
                final String methodName,
                final StaticProfilingConfiguration profilingConfiguration) {
            super(api, access, descriptor, methodVisitor);
            this.methodName = methodName;
            this.profilingConfiguration = profilingConfiguration;
        }

        @Override
        public void visitCode() {
            this.timestampEnterVarIndex = newLocal(Type.LONG_TYPE);

            invokeNanoTime();
            mv.visitVarInsn(LSTORE, this.timestampEnterVarIndex);

            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= IRETURN && opcode <= RETURN) {
                final int timestampExitVarIndex = newLocal(Type.LONG_TYPE);
                invokeNanoTime();
                mv.visitVarInsn(LSTORE, timestampExitVarIndex);

                mv.visitVarInsn(LLOAD, timestampExitVarIndex);
                mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
                mv.visitInsn(LSUB);

                final int elapsedTimeVarIndex = newLocal(Type.LONG_TYPE);
                mv.visitVarInsn(LSTORE, elapsedTimeVarIndex);

                final long minExecutionThreshold = this.profilingConfiguration.getMinExecutionThreshold();
                if (minExecutionThreshold > 0) {
                    mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);
                    mv.visitLdcInsn(minExecutionThreshold);

                    mv.visitInsn(LCMP);

                    final Label jumpLabel = new Label();
                    mv.visitJumpInsn(IFLT, jumpLabel);

                    invokeElapsedTimeRegistration(elapsedTimeVarIndex);

                    mv.visitLabel(jumpLabel);
                } else {
                    invokeElapsedTimeRegistration(elapsedTimeVarIndex);
                }
            }

            super.visitInsn(opcode);
        }

        private void invokeNanoTime() {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
        }

        private void invokeElapsedTimeRegistration(final int elapsedTimeVarIndex) {
            mv.visitLdcInsn(this.methodName);

            mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
            mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);

            final String registerMethodName = this.profilingConfiguration.isDynamicConfigurationEnabled() ? "registerDynamic" : "registerStatic";
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    "ru/joke/profiler/core/output/ExecutionTimeRegistrar",
                    registerMethodName,
                    "(Ljava/lang/String;JJ)V",
                    false
            );
        }
    }
}
