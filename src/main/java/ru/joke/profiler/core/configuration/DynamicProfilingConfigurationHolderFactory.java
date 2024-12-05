package ru.joke.profiler.core.configuration;

public final class DynamicProfilingConfigurationHolderFactory {

    public DynamicProfilingConfigurationHolder create() {
        final DynamicProfilingConfigurationHolder holder = new DynamicProfilingConfigurationHolder();
        holder.init();

        return holder;
    }
}
