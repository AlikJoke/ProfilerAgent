package ru.joke.profiler.core.configuration;

public final class DynamicProfilingConfigurationHolder {

    private static DynamicProfilingConfigurationHolder instance;

    public static DynamicProfilingConfigurationHolder getInstance() {
        return instance;
    }

    DynamicProfilingConfigurationHolder() {
        super();
    }

    private volatile DynamicProfilingConfiguration dynamicConfiguration;

    public void setDynamicConfiguration(final DynamicProfilingConfiguration dynamicConfiguration) {
        this.dynamicConfiguration = dynamicConfiguration;
    }

    public DynamicProfilingConfiguration getDynamicConfiguration() {
        return dynamicConfiguration;
    }

    void init() {
        instance = this;
    }
}
