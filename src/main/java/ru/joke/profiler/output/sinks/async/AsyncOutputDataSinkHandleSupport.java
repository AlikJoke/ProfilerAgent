package ru.joke.profiler.output.sinks.async;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.output.sinks.OutputData;
import ru.joke.profiler.output.sinks.OutputDataSink;
import ru.joke.profiler.output.sinks.OutputDataSinkHandle;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.util.ArgUtil.checkNotNull;

public abstract class AsyncOutputDataSinkHandleSupport<T, C extends AsyncOutputDataSinkConfigurationSupport> implements OutputDataSinkHandle {

    @Override
    public final OutputDataSink<OutputData> create(final Map<String, String> properties) throws Exception {
        final C configuration = loadConfiguration(checkNotNull(properties, "properties"));
        final Map<String, Object> creationContext = buildCreationContext(configuration);
        final AsyncSinkDataFlushingConfiguration asyncConfiguration = configuration.asyncFlushingConfiguration();
        return asyncConfiguration != null && asyncConfiguration.asyncFlushingEnabled()
                ? createAsyncOutputSync(configuration, creationContext, asyncConfiguration)
                : createSyncOutputSink(configuration, creationContext);
    }

    protected C loadConfiguration(final Map<String, String> properties) {
        return ConfigurationParser.parse(configurationType(), properties);
    }

    protected abstract Class<C> configurationType();

    protected Map<String, Object> buildCreationContext(final C configuration) {
        return Collections.emptyMap();
    }

    protected abstract Function<OutputData, Supplier<T>> conversionFunction(
            final C configuration,
            final Map<String, Object> context
    );

    protected abstract OutputDataSink<T> createTerminalOutputSink(
            final C configuration,
            final Map<String, Object> context
    ) throws Exception;

    protected abstract OutputDataSink<OutputData> createSyncOutputSink(
            final C configuration,
            final Map<String, Object> context
    ) throws Exception;

    private OutputDataSink<OutputData> createAsyncOutputSync(
            final C configuration,
            final Map<String, Object> context,
            final AsyncSinkDataFlushingConfiguration asyncConfiguration
    ) throws Exception {
        final OutputDataSink<T> sink = createTerminalOutputSink(configuration, context);
        return new AsyncOutputDataSink<>(
                sink,
                asyncConfiguration,
                conversionFunction(configuration, context),
                type()
        );
    }
}
