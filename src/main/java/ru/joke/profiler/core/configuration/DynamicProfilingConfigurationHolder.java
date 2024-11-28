package ru.joke.profiler.core.configuration;

public final class DynamicProfilingConfigurationHolder {

    private static final DynamicProfilingConfigurationHolder instance = new DynamicProfilingConfigurationHolder();

    public static DynamicProfilingConfigurationHolder getInstance() {
        return instance;
    }

    private volatile DynamicProfilingConfiguration dynamicConfiguration;

    public void setDynamicConfiguration(final DynamicProfilingConfiguration dynamicConfiguration) {
        this.dynamicConfiguration = dynamicConfiguration;
    }

    public DynamicProfilingConfiguration getDynamicConfiguration() {
        return dynamicConfiguration;
    }
}
