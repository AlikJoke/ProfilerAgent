package ru.joke.profiler.core.transformation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.output.ExecutionTimeRegistrarMetadataSelector;

import static org.objectweb.asm.Opcodes.*;

final class ExecutionTimeProfilingVisitor extends ClassVisitor {

    private final String className;
    private final StaticProfilingConfiguration profilingConfiguration;
    private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;

    ExecutionTimeProfilingVisitor(
            final ClassWriter classWriter,
            final String className,
            final StaticProfilingConfiguration profilingConfiguration,
            final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector) {
        super(Opcodes.ASM9, classWriter);
        this.className = className;
        this.profilingConfiguration = profilingConfiguration;
        this.registrarMetadataSelector = registrarMetadataSelector;
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
        return new ExecutionTimeRegistrarVisitor(Opcodes.ASM9, methodAccess, methodDesc, methodVisitor, fullMethodName, profilingConfiguration, registrarMetadataSelector);
    }

    private static class ExecutionTimeRegistrarVisitor extends LocalVariablesSorter {

        private final StaticProfilingConfiguration profilingConfiguration;
        private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;
        private final String methodName;

        private int timestampEnterVarIndex;

        ExecutionTimeRegistrarVisitor(
                final int api,
                final int access,
                final String descriptor,
                final MethodVisitor methodVisitor,
                final String methodName,
                final StaticProfilingConfiguration profilingConfiguration,
                final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector) {
            super(api, access, descriptor, methodVisitor);
            this.methodName = methodName;
            this.profilingConfiguration = profilingConfiguration;
            this.registrarMetadataSelector = registrarMetadataSelector;
        }

        @Override
        public void visitCode() {

            /*
             * ~ long startTime = System.nanoTime();
             */
            this.timestampEnterVarIndex = newLocal(Type.LONG_TYPE);
            invokeNanoTime();
            mv.visitVarInsn(LSTORE, this.timestampEnterVarIndex);

            /*
             * ~ TracedExecutionTimeRegistrar.registerMethodEnter();
             */
            invokeMethodEnterRegistrationIfNeed();

            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            final boolean isTerminalOpcode = opcode >= IRETURN && opcode <= RETURN || opcode == ATHROW;
            if (isTerminalOpcode) {
                insertElapsedTimeRegistrationCall();
            }

            super.visitInsn(opcode);
        }

        private void insertElapsedTimeRegistrationCall() {
            /*
             * ~ long endTime = System.nanoTime();
             */
            final int timestampExitVarIndex = newLocal(Type.LONG_TYPE);
            invokeNanoTime();
            mv.visitVarInsn(LSTORE, timestampExitVarIndex);

            /*
             * ~ long elapsedTime = endTime - startTime;
             */
            mv.visitVarInsn(LLOAD, timestampExitVarIndex);
            mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
            mv.visitInsn(LSUB);

            final int elapsedTimeVarIndex = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, elapsedTimeVarIndex);

            final long minExecutionThreshold = this.profilingConfiguration.getMinExecutionThreshold();
            if (minExecutionThreshold > 0) {

                /*
                 * ~ if (elapsedTime >= minExecutionThreshold) {
                 *       (SimpleExecutionTimeRegistrar | TracedExecutionTimeRegistrar).(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 *   } else {
                 *       // branch exists only if execution tracing enabled
                 *       TracedExecutionTimeRegistrar.registerMethodExit();
                 *   }
                 */
                mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);
                mv.visitLdcInsn(minExecutionThreshold);
                mv.visitInsn(LCMP);

                final Label jumpLabel = new Label();
                mv.visitJumpInsn(IFLT, jumpLabel);

                invokeElapsedTimeRegisterMethod(elapsedTimeVarIndex);
                final Label afterRegistrationCall = new Label();
                mv.visitJumpInsn(GOTO, afterRegistrationCall);

                mv.visitLabel(jumpLabel);

                invokeMethodExitRegistrationIfNeed();

                mv.visitLabel(afterRegistrationCall);
            } else {
                /*
                 * (SimpleExecutionTimeRegistrar | TracedExecutionTimeRegistrar).(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 */
                invokeElapsedTimeRegisterMethod(elapsedTimeVarIndex);
            }
        }

        private void invokeNanoTime() {
            mv.visitMethodInsn(INVOKESTATIC, transformClassName(System.class), "nanoTime", "()J", false);
        }

        private void invokeElapsedTimeRegisterMethod(final int elapsedTimeVarIndex) {
            mv.visitLdcInsn(this.methodName);

            mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
            mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);

            final String registerMethodName = this.registrarMetadataSelector.selectRegistrationMethod();
            final String registerMethodSignature = this.registrarMetadataSelector.selectTimeRegistrationMethodSignature();
            final String registrarClass = transformClassName(this.registrarMetadataSelector.selectRegistrarClass());
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    registrarClass,
                    registerMethodName,
                    registerMethodSignature,
                    false
            );
        }

        private String transformClassName(final Class<?> cls) {
            return cls.getCanonicalName().replace('.', '/');
        }

        private void invokeMethodExitRegistrationIfNeed() {
            final String exitRegistrationMethod = this.registrarMetadataSelector.selectExitRegistrationMethod();
            invokeMethodVisitRegistrationIfNeed(exitRegistrationMethod, this.registrarMetadataSelector.selectExitRegistrationMethodSignature());
        }

        private void invokeMethodEnterRegistrationIfNeed() {
            final String enterRegistrationMethod = this.registrarMetadataSelector.selectEnterRegistrationMethod();
            invokeMethodVisitRegistrationIfNeed(enterRegistrationMethod, this.registrarMetadataSelector.selectEnterRegistrationMethodSignature());
        }

        private void invokeMethodVisitRegistrationIfNeed(final String methodName, final String signature) {
            if (methodName == null) {
                return;
            }

            mv.visitMethodInsn(
                    INVOKESTATIC,
                    transformClassName(this.registrarMetadataSelector.selectRegistrarClass()),
                    methodName,
                    signature,
                    false
            );
        }
    }
}
