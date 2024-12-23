package ru.joke.profiler.output.handlers.async;

import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.OutputDataSinkHandle;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static ru.joke.profiler.configuration.ConfigurationProperties.ASYNC_FLUSHING_ENABLED;
import static ru.joke.profiler.configuration.ConfigurationProperties.parseBooleanProperty;

public abstract class AsyncOutputDataSinkHandleSupport<T> implements OutputDataSinkHandle {

    @Override
    public final OutputDataSink<OutputData> create(final Map<String, String> properties) throws Exception {
        final boolean asyncFlushingEnabled = parseBooleanProperty(properties, ASYNC_FLUSHING_ENABLED);
        final Map<String, Object> creationContext = buildCreationContext(properties);
        return asyncFlushingEnabled
                ? createAsyncOutputSync(properties, creationContext)
                : createSyncOutputSink(properties, creationContext);
    }

    protected Map<String, Object> buildCreationContext(final Map<String, String> properties) {
        return Collections.emptyMap();
    }

    protected abstract Function<OutputData, Supplier<T>> conversionFunction(
            final Map<String, String> properties,
            final Map<String, Object> context
    );

    protected abstract OutputDataSink<T> createTerminalOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception;

    protected abstract OutputDataSink<OutputData> createSyncOutputSink(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception;

    protected OutputDataSink<OutputData> createAsyncOutputSync(
            final Map<String, String> properties,
            final Map<String, Object> context
    ) throws Exception {
        final OutputDataSink<T> sink = createTerminalOutputSink(properties, context);
        final AsyncSinkDataFlushingConfiguration configuration = composeConfiguration(properties);
        return new AsyncOutputDataSink<>(sink, configuration, conversionFunction(properties, context));
    }

    private AsyncSinkDataFlushingConfiguration composeConfiguration(final Map<String, String> properties) {
        final AsyncOutputSinkFlushingConfigurationLoader configurationLoader = new AsyncOutputSinkFlushingConfigurationLoader();
        return configurationLoader.load(properties);
    }
}
