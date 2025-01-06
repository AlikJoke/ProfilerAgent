package ru.joke.profiler.output.sinks.util.pool;

public interface ConnectionFactory<T extends PooledConnection> {

    T create();
}