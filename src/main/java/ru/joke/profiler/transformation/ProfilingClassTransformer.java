package ru.joke.profiler.transformation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.output.ExecutionTimeRegistrarMetadataSelector;
import ru.joke.profiler.transformation.spy.SpyInjector;

import static org.objectweb.asm.Opcodes.*;
import static ru.joke.profiler.util.BytecodeUtil.*;

final class ProfilingClassTransformer extends ClassVisitor {

    private static final String SYSTEM_CLASS_NAME = toBytecodeFormat(System.class);
    private static final String NANO_TIME_METHOD_NAME = "nanoTime";
    private static final String NANO_TIME_METHOD_SIGNATURE = buildMethodDescriptor(System.class, NANO_TIME_METHOD_NAME);

    private final String className;
    private final StaticProfilingConfiguration profilingConfiguration;
    private final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector;
    private final NativeClassMethodsCollector nativeClassMethodsCollector;
    private final SpyInjector spyInjector;

    ProfilingClassTransformer(
            final ClassWriter classWriter,
            final String className,
            final StaticProfilingConfiguration profilingConfiguration,
            final ExecutionTimeRegistrarMetadataSelector registrarMetadataSelector,
            final NativeClassMethodsCollector nativeClassMethodsCollector,
            final SpyInjector spyInjector
    ) {
        super(Opcodes.ASM9, classWriter);
        this.className = className;
        this.profilingConfiguration = profilingConfiguration;
        this.registrarMetadataSelector = registrarMetadataSelector;
        this.nativeClassMethodsCollector = nativeClassMethodsCollector;
        this.spyInjector = spyInjector;
    }

    @Override
    public MethodVisitor visitMethod(
            final int methodAccess,
            final String methodName,
            final String methodDesc,
            final String signature,
            final String[] exceptions
    ) {
        final MethodVisitor methodVisitor = this.cv.visitMethod(
                methodAccess,
                methodName,
                methodDesc,
                signature,
                exceptions
        );

        if ((methodAccess & ACC_NATIVE) != 0 || (methodAccess & ACC_ABSTRACT) != 0) {
            return methodVisitor;
        }

        final String fullMethodName = this.className + "." + methodName;
        if (!this.profilingConfiguration.isResourceMustBeProfiled(fullMethodName)) {
            return methodVisitor;
        }

        return new MethodExecutionTimeRegistrationTransformer(
                Opcodes.ASM9,
                methodAccess,
                methodDesc,
                methodVisitor,
                fullMethodName
        );
    }

    private class MethodExecutionTimeRegistrationTransformer extends LocalVariablesSorter {

        private final String methodName;
        private final boolean isConstructor;

        private int timestampEnterVarIndex;

        private Label tryStartLabel;
        private Label tryHandlerLabel;
        private Label lastThrowLabel;

        private int lastInstruction;

        MethodExecutionTimeRegistrationTransformer(
                final int api,
                final int access,
                final String descriptor,
                final MethodVisitor methodVisitor,
                final String methodName
        ) {
            super(api, access, descriptor, methodVisitor);
            this.methodName = methodName;
            this.isConstructor = methodName.endsWith(CONSTRUCTOR_NAME);
        }

        @Override
        public void visitCode() {
            /*
             * ~ ExecutionTimeRegistrar.getInstance().registerMethodEnter();
             */
            invokeMethodEnterRegistration(this.methodName);

            /*
             * ~ long startTime = System.nanoTime();
             */
            this.timestampEnterVarIndex = newLocal(Type.LONG_TYPE);
            invokeNanoTime();
            mv.visitVarInsn(LSTORE, this.timestampEnterVarIndex);

            /*
             * Try block in constructors starts only after the parent class constructor is called.
             */
            if (!this.isConstructor) {
                injectTryBlockBeginning(
                        this.methodName,
                        this.timestampEnterVarIndex,
                        this.tryHandlerLabel = new Label(),
                        this.tryStartLabel = new Label()
                );
            }

            super.visitCode();
        }

        @Override
        public void visitMethodInsn(
                final int opcode,
                final String owner,
                final String name,
                final String descriptor,
                final boolean isInterface
        ) {
            final boolean isNativeMethod = nativeClassMethodsCollector.isNativeMethod(owner, name, descriptor);
            if (isNativeMethod) {
                injectNativeMethodExecutionRegistration(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );
            } else if (!spyInjector.injectSpy(this.mv, owner, name, descriptor)) {
                super.visitMethodInsn(
                        opcode,
                        owner,
                        name,
                        descriptor,
                        isInterface
                );
            }

            if (name.equals(CONSTRUCTOR_NAME)
                    && this.isConstructor
                    && opcode == INVOKESPECIAL
                    && this.tryStartLabel == null
                    && this.lastInstruction != DUP) {
                injectTryBlockBeginning(
                        this.methodName,
                        this.timestampEnterVarIndex,
                        this.tryHandlerLabel = new Label(),
                        this.tryStartLabel = new Label()
                );
            } else {
                this.lastInstruction = opcode;
            }
        }

        @Override
        public void visitInsn(int opcode) {
            this.lastInstruction = opcode;
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
        public void visitMaxs(int maxStack, int maxLocals) {
            if (this.lastThrowLabel != null && this.lastThrowLabel.getOffset() > this.tryStartLabel.getOffset()) {
                mv.visitTryCatchBlock(this.tryStartLabel, this.lastThrowLabel, this.tryHandlerLabel, null);
            }

            super.visitMaxs(maxStack, maxLocals);
        }

        private void injectNativeMethodExecutionRegistration(
                final int opcode,
                final String owner,
                final String name,
                final String descriptor,
                final boolean isInterface
        ) {
            final String nativeMethodName = toCanonicalFormat(owner) + '.' + name;
            /*
             * ~ ExecutionTimeRegistrar.getInstance().registerMethodEnter();
             */
            invokeMethodEnterRegistration(nativeMethodName);

            /*
             * ~ long startTime = System.nanoTime();
             */
            final int nativeMethodStartVarIndex = newLocal(Type.LONG_TYPE);
            invokeNanoTime();
            mv.visitVarInsn(LSTORE, nativeMethodStartVarIndex);

            final Label tryHandlerLabel = new Label();
            final Label tryStartLabel = new Label();
            injectTryBlockBeginning(
                    nativeMethodName,
                    nativeMethodStartVarIndex,
                    tryHandlerLabel,
                    tryStartLabel
            );

            super.visitMethodInsn(
                    opcode,
                    owner,
                    name,
                    descriptor,
                    isInterface
            );

            final Label tryEndLabel = new Label();
            mv.visitLabel(tryEndLabel);

            insertElapsedTimeRegistrationCall(nativeMethodName, nativeMethodStartVarIndex);

            mv.visitTryCatchBlock(tryStartLabel, tryEndLabel, tryHandlerLabel, null);
        }

        private void onSuccessMethodExecution(final int returnOpcode) {
            final Label tryEndLabel = new Label();
            mv.visitLabel(tryEndLabel);

            insertElapsedTimeRegistrationCall(this.methodName, this.timestampEnterVarIndex);

            super.visitInsn(returnOpcode);

            if (this.tryStartLabel.getOffset() < tryEndLabel.getOffset()) {
                mv.visitTryCatchBlock(this.tryStartLabel, tryEndLabel, this.tryHandlerLabel, null);
            }

            this.tryStartLabel = new Label();
            mv.visitLabel(this.tryStartLabel);
        }

        private void injectTryBlockBeginning(
                final String instrumentedMethodName,
                final int timestampEnterVarIndex,
                final Label tryHandlerLabel,
                final Label tryStartLabel
        ) {
            final Label codeStart = new Label();
            mv.visitJumpInsn(GOTO, codeStart);

            injectTryHandler(instrumentedMethodName, timestampEnterVarIndex, tryHandlerLabel);

            mv.visitLabel(codeStart);
            mv.visitLabel(tryStartLabel);
        }

        private void injectTryHandler(
                final String instrumentedMethodName,
                final int timestampEnterVarIndex,
                final Label tryHandlerLabel
        ) {
            mv.visitLabel(tryHandlerLabel);
            insertElapsedTimeRegistrationCall(instrumentedMethodName, timestampEnterVarIndex);
            mv.visitInsn(ATHROW);
        }

        private void insertElapsedTimeRegistrationCall(
                final String instrumentedMethodName,
                final int timestampEnterVarIndex
        ) {
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
            mv.visitVarInsn(LLOAD, timestampEnterVarIndex);
            mv.visitInsn(LSUB);

            final int elapsedTimeVarIndex = newLocal(Type.LONG_TYPE);
            mv.visitVarInsn(LSTORE, elapsedTimeVarIndex);

            final long minExecutionThreshold = profilingConfiguration.minExecutionThresholdNs();
            if (minExecutionThreshold > 0) {

                /*
                 * ~ if (elapsedTime >= minExecutionThreshold) {
                 *       ExecutionTimeRegistrar.getInstance().(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 *   } else {
                 *       ExecutionTimeRegistrar.getInstance().registerMethodExit();
                 *   }
                 */
                mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);
                mv.visitLdcInsn(minExecutionThreshold);
                mv.visitInsn(LCMP);

                final Label jumpLabel = new Label();
                mv.visitJumpInsn(IFLT, jumpLabel);

                invokeMethodExitRegistration(instrumentedMethodName, elapsedTimeVarIndex, timestampEnterVarIndex);
                final Label afterRegistrationCall = new Label();
                mv.visitJumpInsn(GOTO, afterRegistrationCall);

                mv.visitLabel(jumpLabel);

                invokeMethodExitRegistration(instrumentedMethodName);

                mv.visitLabel(afterRegistrationCall);
            } else {
                /*
                 * ExecutionTimeRegistrar.getInstance().(registerDynamic(this.method, startTime, elapsedTime) | registerStatic(this.method, startTime, elapsedTime));
                 */
                invokeMethodExitRegistration(
                        instrumentedMethodName,
                        elapsedTimeVarIndex,
                        timestampEnterVarIndex
                );
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

        private void invokeMethodExitRegistration(
                final String instrumentedMethodName,
                final int elapsedTimeVarIndex,
                final int timestampEnterVarIndex
        ) {
            final String registerMethodName = registrarMetadataSelector.selectExitTimeRegistrationMethod();
            final String registerMethodSignature = registrarMetadataSelector.selectTimeRegistrationMethodSignature();
            final String registrarClass = registrarMetadataSelector.selectRegistrarClass();
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    registrarClass,
                    registrarMetadataSelector.selectRegistrarSingletonAccessorMethod(),
                    registrarMetadataSelector.selectRegistrarSingletonAccessorSignature(),
                    false
            );

            mv.visitLdcInsn(instrumentedMethodName);

            mv.visitVarInsn(LLOAD, timestampEnterVarIndex);
            mv.visitVarInsn(LLOAD, elapsedTimeVarIndex);

            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    registrarClass,
                    registerMethodName,
                    registerMethodSignature,
                    false
            );
        }

        private void invokeMethodExitRegistration(final String instrumentedMethodName) {
            final String exitRegistrationMethod = registrarMetadataSelector.selectExitRegistrationMethod();
            invokeMethodVisitRegistration(exitRegistrationMethod, registrarMetadataSelector.selectExitRegistrationMethodSignature(), false, instrumentedMethodName);
        }

        private void invokeMethodEnterRegistration(final String instrumentedMethodName) {
            final String enterRegistrationMethod = registrarMetadataSelector.selectEnterRegistrationMethod();
            invokeMethodVisitRegistration(enterRegistrationMethod, registrarMetadataSelector.selectEnterRegistrationMethodSignature(), true, instrumentedMethodName);
        }

        private void invokeMethodVisitRegistration(
                final String visitMethodName,
                final String signature,
                final boolean loadInstrumentedMethodNameAsParameter,
                final String instrumentedMethodName
        ) {
            final String registrarClass = registrarMetadataSelector.selectRegistrarClass();
            mv.visitMethodInsn(
                    INVOKESTATIC,
                    registrarClass,
                    registrarMetadataSelector.selectRegistrarSingletonAccessorMethod(),
                    registrarMetadataSelector.selectRegistrarSingletonAccessorSignature(),
                    false
            );

            if (loadInstrumentedMethodNameAsParameter) {
                mv.visitLdcInsn(instrumentedMethodName);
            }

            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    registrarClass,
                    visitMethodName,
                    signature,
                    false
            );
        }
    }
}
