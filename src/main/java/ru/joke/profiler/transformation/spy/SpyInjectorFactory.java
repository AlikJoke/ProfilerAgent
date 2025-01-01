package ru.joke.profiler.transformation.spy;

import org.objectweb.asm.MethodVisitor;

import java.util.*;
import java.util.stream.Collectors;

public interface SpyInjectorFactory {

    String spyId();

    SpyInjector createSpyInjector(SpyContext context);

    static SpyInjector create(SpyContext context) {
        final Map<String, SpyInjectorFactory> factories = new HashMap<>();
        for (final SpyInjectorFactory factory : ServiceLoader.load(SpyInjectorFactory.class)) {
            factories.put(factory.spyId().toLowerCase(), factory);
        }

        final List<String> activeSpies = context.staticConfiguration().spies();
        final List<SpyInjector> injectors =
                activeSpies
                        .stream()
                        .map(String::toLowerCase)
                        .map(factories::get)
                        .filter(Objects::nonNull)
                        .map(factory -> factory.createSpyInjector(context))
                        .collect(Collectors.toList());

        if (!injectors.isEmpty()) {
            return injectors.size() == 1 ? injectors.get(0) : new CompositeSpyInjector(injectors);
        }

        return (mv, owner, method, descriptor) -> false;
    }

    final class CompositeSpyInjector implements SpyInjector {

        private final List<SpyInjector> injectors;

        CompositeSpyInjector(final List<SpyInjector> injectors) {
            this.injectors = Collections.unmodifiableList(injectors);
        }

        @Override
        public boolean injectSpy(
                final MethodVisitor mv,
                final String owner,
                final String method,
                final String descriptor
        ) {
            for (final SpyInjector injector : this.injectors) {
                if (injector.injectSpy(mv, owner, method, descriptor)) {
                    return true;
                }
            }

            return false;
        }
    }
}
