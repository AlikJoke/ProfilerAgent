package ru.joke.profiler.core.transformation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import ru.joke.profiler.core.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.core.output.ExecutionTimeRegistrarMetadataSelector;

import static org.objectweb.asm.Opcodes.*;

public final class ClassProfilingTransformer extends ClassVisitor {

    private final String className;
    private final StaticProfilingConfiguration profilingConfiguration;
    private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;

    ClassProfilingTransformer(
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
        if ((methodAccess & ACC_NATIVE) != 0 || (methodAccess & ACC_ABSTRACT) != 0) {
            return null;
        }

        final MethodVisitor methodVisitor = this.cv.visitMethod(methodAccess, methodName, methodDesc, signature, exceptions);
        final String fullMethodName = this.className + "." + methodName;
        return new MethodExecutionTimeRegistrationTransformer(Opcodes.ASM9, methodAccess, methodDesc, methodVisitor, fullMethodName, profilingConfiguration, registrarMetadataSelector);
    }

    private static class MethodExecutionTimeRegistrationTransformer extends LocalVariablesSorter {

        private static final String SYSTEM_CLASS_NAME = System.class.getCanonicalName().replace('.', '/');
        private static final String NANO_TIME_METHOD_NAME = "nanoTime";
        private static final String NANO_TIME_METHOD_SIGNATURE = "()J";

        private static final String CONSTRUCTOR = "<init>";

        private final StaticProfilingConfiguration profilingConfiguration;
        private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;
        private final String methodName;
        private final boolean isConstructor;

        private int timestampEnterVarIndex;

        private Label tryStartLabel, tryHandlerLabel, lastThrowLabel;

        MethodExecutionTimeRegistrationTransformer(
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
            this.isConstructor = methodName.endsWith(CONSTRUCTOR);
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
             * ~ ExecutionTimeRegistrar.getInstance().registerMethodEnter();
             */
            invokeMethodEnterRegistration();

            /*
             * Try block in constructors starts only after the parent class constructor is called.
             */
            if (!this.isConstructor) {
                injectTryBlockBeginning();
            }

            super.visitCode();
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
            if (name.equals(CONSTRUCTOR) && this.isConstructor && opcode == INVOKESPECIAL && this.tryStartLabel == null) {
                injectTryBlockBeginning();
            }
        }

        @Override
        public void visitInsn(int opcode) {
            if (opcode >= IRETURN && opcode <= RETURN) {
                onSuccessMethodExecution(opcode);
                return;
            }

            super.visitInsn(opcode);
            if (opcode == Opcodes.ATHROW) {
                this.lastThrowLabel = new Label();
                mv.visitLabel(this.lastThrowLabel);
            }
        }

        @Override
        public void visitEnd() {
            if (this.lastThrowLabel != null && this.lastThrowLabel.getOffset() > this.tryStartLabel.getOffset()) {
                mv.visitTryCatchBlock(this.tryStartLabel, this.lastThrowLabel, this.tryHandlerLabel, null);
            }
        }

        private void onSuccessMethodExecution(final int returnOpcode) {
            final Label tryEndLabel = new Label();
            mv.visitLabel(tryEndLabel);

            insertElapsedTimeRegistrationCall();

            super.visitInsn(returnOpcode);

            if (this.tryStartLabel.getOffset() < tryEndLabel.getOffset()) {
                mv.visitTryCatchBlock(this.tryStartLabel, tryEndLabel, this.tryHandlerLabel, null);
            }

            this.tryStartLabel = new Label();
            mv.visitLabel(this.tryStartLabel);
        }

        private void injectTryBlockBeginning() {
            final Label codeStart = new Label();
            mv.visitJumpInsn(GOTO, codeStart);

            injectTryHandler();

            mv.visitLabel(codeStart);

            this.tryStartLabel = new Label();
            mv.visitLabel(this.tryStartLabel);
        }

        private void injectTryHandler() {
            this.tryHandlerLabel = new Label();
            mv.visitLabel(this.tryHandlerLabel);
            insertElapsedTimeRegistrationCall();
            mv.visitInsn(ATHROW);
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
                 *       ExecutionTimeRegistrar.getInstance().(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 *   } else {
                 *       // branch exists only if execution tracing enabled
                 *       ExecutionTimeRegistrar.getInstance().registerMethodExit();
                 *   }
                 */
                mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);
                mv.visitLdcInsn(minExecutionThreshold);
                mv.visitInsn(LCMP);

                final Label jumpLabel = new Label();
                mv.visitJumpInsn(IFLT, jumpLabel);

                invokeMethodExitRegistration(elapsedTimeVarIndex);
                final Label afterRegistrationCall = new Label();
                mv.visitJumpInsn(GOTO, afterRegistrationCall);

                mv.visitLabel(jumpLabel);

                invokeMethodExitRegistration();

                mv.visitLabel(afterRegistrationCall);
            } else {
                /*
                 * ExecutionTimeRegistrar.getInstance().(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 */
                invokeMethodExitRegistration(elapsedTimeVarIndex);
            }
        }

        private void invokeNanoTime() {
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    SYSTEM_CLASS_NAME,
                    NANO_TIME_METHOD_NAME,
                    NANO_TIME_METHOD_SIGNATURE,
                    false
            );
        }

        private void invokeMethodExitRegistration(final int elapsedTimeVarIndex) {
            final String registerMethodName = this.registrarMetadataSelector.selectExitTimeRegistrationMethod();
            final String registerMethodSignature = this.registrarMetadataSelector.selectTimeRegistrationMethodSignature();
            final String registrarClass = this.registrarMetadataSelector.selectRegistrarClass();
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    registrarClass,
                    this.registrarMetadataSelector.selectRegistrarSingletonAccessorMethod(),
                    this.registrarMetadataSelector.selectRegistrarSingletonAccessorSignature(),
                    false
            );

            mv.visitLdcInsn(this.methodName);

            mv.visitVarInsn(LLOAD, this.timestampEnterVarIndex);
            mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);

            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    registrarClass,
                    registerMethodName,
                    registerMethodSignature,
                    false
            );
        }

        private void invokeMethodExitRegistration() {
            final String exitRegistrationMethod = this.registrarMetadataSelector.selectExitRegistrationMethod();
            invokeMethodVisitRegistration(exitRegistrationMethod, this.registrarMetadataSelector.selectExitRegistrationMethodSignature());
        }

        private void invokeMethodEnterRegistration() {
            final String enterRegistrationMethod = this.registrarMetadataSelector.selectEnterRegistrationMethod();
            invokeMethodVisitRegistration(enterRegistrationMethod, this.registrarMetadataSelector.selectEnterRegistrationMethodSignature());
        }

        private void invokeMethodVisitRegistration(final String methodName, final String signature) {
            final String registrarClass = this.registrarMetadataSelector.selectRegistrarClass();
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    registrarClass,
                    this.registrarMetadataSelector.selectRegistrarSingletonAccessorMethod(),
                    this.registrarMetadataSelector.selectRegistrarSingletonAccessorSignature(),
                    false
            );
            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    registrarClass,
                    methodName,
                    signature,
                    false
            );
        }
    }
}
