package ru.joke.profiler.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public final class DynamicProfilingConfigurationHolder implements Supplier<DynamicProfilingConfiguration> {

    private static DynamicProfilingConfigurationHolder instance;

    public static DynamicProfilingConfigurationHolder getInstance() {
        return instance;
    }

    private final Map<String, BiConsumer<String, DynamicProfilingConfiguration>> subscriptions;
    private volatile DynamicProfilingConfiguration dynamicConfiguration;

    DynamicProfilingConfigurationHolder() {
        this.subscriptions = new ConcurrentHashMap<>();
    }

    public void set(final DynamicProfilingConfiguration dynamicConfiguration) {
        this.dynamicConfiguration = dynamicConfiguration;
        this.subscriptions.forEach((id, s) -> s.accept(id, dynamicConfiguration));
    }

    @Override
    public DynamicProfilingConfiguration get() {
        return dynamicConfiguration;
    }

    public void subscribeOnChanges(
            final String subscriptionId,
            final BiConsumer<String, DynamicProfilingConfiguration> subscription
    ) {
        this.subscriptions.put(subscriptionId, subscription);
    }

    public boolean unsubscribe(final String subscriptionId) {
        return this.subscriptions.remove(subscriptionId) != null;
    }

    void init() {
        instance = this;
    }
}
