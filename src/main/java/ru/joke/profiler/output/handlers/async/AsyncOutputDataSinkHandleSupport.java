package ru.joke.profiler.output.handlers.async;

import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.output.handlers.OutputData;
import ru.joke.profiler.output.handlers.OutputDataSink;
import ru.joke.profiler.output.handlers.OutputDataSinkHandle;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AsyncOutputDataSinkHandleSupport<T> implements OutputDataSinkHandle {

    @Override
    public final OutputDataSink<OutputData> create(final Map<String, String> properties) throws Exception {
        final AsyncSinkDataFlushingConfiguration asyncConfiguration = ConfigurationParser.parse(AsyncSinkDataFlushingConfiguration.class, properties);
        final Map<String, Object> creationContext = buildCreationContext(properties);
        return asyncConfiguration.asyncFlushingEnabled()
                ? createAsyncOutputSync(properties, creationContext, asyncConfiguration)
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

    private OutputDataSink<OutputData> createAsyncOutputSync(
            final Map<String, String> properties,
            final Map<String, Object> context,
            final AsyncSinkDataFlushingConfiguration asyncConfiguration
    ) throws Exception {
        final OutputDataSink<T> sink = createTerminalOutputSink(properties, context);
        return new AsyncOutputDataSink<>(
                sink,
                asyncConfiguration,
                conversionFunction(properties, context)
        );
    }
}
