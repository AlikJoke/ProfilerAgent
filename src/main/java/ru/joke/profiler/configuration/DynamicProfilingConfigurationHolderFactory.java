package ru.joke.profiler.configuration;

public final class DynamicProfilingConfigurationHolderFactory {

    public DynamicProfilingConfigurationHolder create() {
        final DynamicProfilingConfigurationHolder holder = new DynamicProfilingConfigurationHolder();
        holder.init();

        return holder;
    }
}
