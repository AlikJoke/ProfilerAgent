package ru.joke.profiler.output.sinks;

import ru.joke.profiler.configuration.InvalidConfigurationException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class OutputDataSinkFactory {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    public OutputDataSink<OutputData> create(
            final List<String> types,
            final boolean ignoreSinkErrors,
            final Map<String, String> properties
    ) throws Exception {
        final Map<String, OutputDataSinkHandle> sinkHandles = new HashMap<>();
        for (final OutputDataSinkHandle handle : ServiceLoader.load(OutputDataSinkHandle.class)) {
            sinkHandles.put(handle.type().toLowerCase(), handle);
        }

        final List<OutputDataSink<OutputData>> sinks = new ArrayList<>();
        for (final String type : types) {
            final OutputDataSinkHandle handle = sinkHandles.get(type.toLowerCase());
            if (handle == null) {
                throw new InvalidConfigurationException(String.format("Unknown sink type provided: %s", type));
            }

            sinks.add(handle.create(properties));
        }

        return sinks.size() == 1
                ? ignoreSinkErrors
                    ? new NoErrorOutputDataSinkWrapper(sinks.get(0))
                    : sinks.get(0)
                : new CompositeOutputDataSink(sinks, ignoreSinkErrors);
    }

    private static class NoErrorOutputDataSinkWrapper implements OutputDataSink<OutputData> {

        private final OutputDataSink<OutputData> sink;

        private NoErrorOutputDataSinkWrapper(final OutputDataSink<OutputData> sink) {
            this.sink = sink;
        }

        @Override
        public void init() {
            this.sink.init();
        }

        @Override
        public void write(OutputData dataItem) {
            try {
                sink.write(dataItem);
            } catch (RuntimeException ex) {
                logger.log(Level.INFO, "Unable to write data to sink", ex);
            }
        }

        @Override
        public void write(List<OutputData> dataItems) {
            try {
                sink.write(dataItems);
            } catch (RuntimeException ex) {
                logger.log(Level.INFO, "Unable to write data to sink", ex);
            }
        }

        @Override
        public void close() {
            sink.close();
        }
    }

    private static class CompositeOutputDataSink implements OutputDataSink<OutputData> {

        private final List<OutputDataSink<OutputData>> delegateSinks;
        private final boolean ignoreSinkErrors;

        private CompositeOutputDataSink(
                final List<OutputDataSink<OutputData>> delegateSinks,
                final boolean ignoreSinkErrors
        ) {
            this.delegateSinks = delegateSinks;
            this.ignoreSinkErrors = ignoreSinkErrors;
        }

        @Override
        public void init() {
            this.delegateSinks.forEach(OutputDataSink::init);
        }

        @Override
        public void write(OutputData dataItem) {
            this.delegateSinks.forEach(s -> {
                try {
                    s.write(dataItem);
                } catch (RuntimeException ex) {
                    if (!this.ignoreSinkErrors) {
                        throw ex;
                    }

                    logger.log(Level.INFO, "Unable to write data to sink", ex);
                }
            });
        }

        @Override
        public void write(List<OutputData> dataItems) {
            this.delegateSinks.forEach(s -> {
                try {
                    s.write(dataItems);
                } catch (RuntimeException ex) {
                    if (!this.ignoreSinkErrors) {
                        throw ex;
                    }
                    logger.log(Level.INFO, "Unable to write data to sink", ex);
                }
            });
        }

        @Override
        public void close() {
            this.delegateSinks.forEach(OutputDataSink::close);
        }
    }
}
