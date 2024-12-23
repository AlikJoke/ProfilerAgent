package ru.joke.profiler.output.handlers.jdbc;

import java.sql.Connection;

public interface ConnectionPool extends AutoCloseable {

    void init();

    void release(Connection connection);

    Connection get();

    @Override
    void close();
}
