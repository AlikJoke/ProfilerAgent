package ru.joke.profiler.configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Logger;

public final class DynamicProfilingConfigurationHolder implements Supplier<DynamicProfilingConfiguration> {

    private static final Logger logger = Logger.getLogger(DynamicProfilingConfiguration.class.getCanonicalName());

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
        this.subscriptions.forEach((id, s) -> {
            logger.finest(String.format("Calling to subscription '%s' with config: %s", id, dynamicConfiguration));
            s.accept(id, dynamicConfiguration);
        });
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
        logger.fine(String.format("Subscription is created with id %s", subscription));
    }

    public boolean unsubscribe(final String subscriptionId) {
        final boolean result = this.subscriptions.remove(subscriptionId) != null;
        logger.fine(String.format("Unsubscribe called for %s with result %b", subscriptionId, result));

        return result;
    }

    void init() {
        instance = this;
    }
}
