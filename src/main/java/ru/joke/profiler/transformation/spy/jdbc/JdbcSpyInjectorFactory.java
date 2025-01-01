package ru.joke.profiler.transformation.spy.jdbc;

import ru.joke.profiler.transformation.spy.SpyContext;
import ru.joke.profiler.transformation.spy.SpyInjector;
import ru.joke.profiler.transformation.spy.SpyInjectorFactory;

public final class JdbcSpyInjectorFactory implements SpyInjectorFactory {

    static final String SPY_ID = "jdbc";

    @Override
    public String spyId() {
        return SPY_ID;
    }

    @Override
    public SpyInjector createSpyInjector(final SpyContext context) {
        return new JdbcSpyInjector(context);
    }
}
