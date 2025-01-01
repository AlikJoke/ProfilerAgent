package ru.joke.profiler.transformation.spy.jdbc;

import org.objectweb.asm.MethodVisitor;
import ru.joke.profiler.transformation.spy.SpyContext;
import ru.joke.profiler.transformation.spy.SpyInjector;

import java.sql.Connection;

import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static ru.joke.profiler.util.BytecodeUtil.insertObjectParameterInTheBeginningOfMethodDescriptor;
import static ru.joke.profiler.util.BytecodeUtil.toBytecodeFormat;

final class JdbcSpyInjector implements SpyInjector {

    private static final String CONNECTION_CLASS_BYTECODE_FORMAT = toBytecodeFormat(Connection.class);

    private static final String SIMPLE_STATEMENT_METHOD_FACTORY = "createStatement";
    private static final String PREPARED_STATEMENT_METHOD_FACTORY = "prepareStatement";
    private static final String CALLABLE_STATEMENT_METHOD_FACTORY = "prepareCall";

    private final String factoryFullName;

    JdbcSpyInjector(final SpyContext context) {
        final JdbcStatementFactory factory = JdbcStatementFactory.create(context);
        this.factoryFullName = toBytecodeFormat(factory.getClass());
    }

    @Override
    public boolean injectSpy(
            final MethodVisitor mv,
            final String owner,
            final String method,
            final String descriptor
    ) {
        final boolean applied = owner.equals(CONNECTION_CLASS_BYTECODE_FORMAT)
                && (method.equals(PREPARED_STATEMENT_METHOD_FACTORY) || method.equals(SIMPLE_STATEMENT_METHOD_FACTORY) || method.equals(CALLABLE_STATEMENT_METHOD_FACTORY));
        if (!applied) {
            return false;
        }

        mv.visitMethodInsn(
                INVOKESTATIC,
                this.factoryFullName,
                method,
                insertObjectParameterInTheBeginningOfMethodDescriptor(descriptor, owner),
                false
        );

        return true;
    }
}
