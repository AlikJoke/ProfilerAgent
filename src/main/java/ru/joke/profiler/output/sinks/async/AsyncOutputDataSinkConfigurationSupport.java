package ru.joke.profiler.output.sinks.async;

public abstract class AsyncOutputDataSinkConfigurationSupport {

    protected final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration;

    protected AsyncOutputDataSinkConfigurationSupport(final AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration) {
        this.asyncFlushingConfiguration = asyncFlushingConfiguration;
    }

    public AsyncSinkDataFlushingConfiguration asyncFlushingConfiguration() {
        return asyncFlushingConfiguration;
    }
}
