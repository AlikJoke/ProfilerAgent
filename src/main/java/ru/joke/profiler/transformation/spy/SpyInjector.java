package ru.joke.profiler.transformation.spy;

import org.objectweb.asm.MethodVisitor;

public interface SpyInjector {

    boolean injectSpy(
            MethodVisitor mv,
            String owner,
            String method,
            String descriptor
    );
}
