package ru.joke.profiler.output.handlers.util.pool;

public interface ConnectionFactory<T extends PooledConnection> {

    T create();
}