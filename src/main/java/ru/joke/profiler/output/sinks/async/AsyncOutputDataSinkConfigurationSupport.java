package ru.joke.profiler.output.sinks.async;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public abstract class AsyncOutputDataSinkConfigurationSupport {

    protected final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration;

    protected AsyncOutputDataSinkConfigurationSupport(final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration) {
        this.asyncFlushingConfiguration = checkNotNull(asyncFlushingConfiguration, "asyncFlushingConfiguration");
    }

    public AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration() {
        return asyncFlushingConfiguration;
    }
}
