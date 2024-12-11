package ru.joke.profiler.core.output.handlers.stream;

import ru.joke.profiler.core.ProfilerException;
import ru.joke.profiler.core.output.handlers.OutputData;
import ru.joke.profiler.core.output.handlers.OutputDataSink;
import ru.joke.profiler.core.output.handlers.OutputStringDataFormatter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class OutputDataAbsStreamSink implements OutputDataSink {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final String FLUSHING_THREAD_NAME = "profiler-flushing-thread";

    private final ScheduledExecutorService flushExecutor;

    private final Writer writer;
    private final OutputStringDataFormatter formatter;
    private final long flushInterval;

    protected OutputDataAbsStreamSink(
            final PrintStream pw,
            final OutputStringDataFormatter formatter,
            final int bufferSize,
            final long flushInterval) {
        this.writer = new BufferedWriter(new OutputStreamWriter(pw, StandardCharsets.UTF_8), bufferSize);
        this.formatter = formatter;
        this.flushExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            final Thread thread = new Thread(r);
            thread.setDaemon(true);
            thread.setName(FLUSHING_THREAD_NAME);
            thread.setUncaughtExceptionHandler((t, e) -> logger.log(Level.SEVERE, "Unable to flush data", e));

            return thread;
        });
        this.flushInterval = flushInterval;
    }

    @Override
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
        this.flushExecutor.scheduleAtFixedRate(this::flush, this.flushInterval, this.flushInterval, TimeUnit.MILLISECONDS);
    }

    @Override
    public void write(final OutputData outputData) {
        final String outputString = this.formatter.format(outputData);

        try {
            this.writer.write(outputString);
            this.writer.write(System.lineSeparator());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to write to sink", ex);
            throw new ProfilerException(ex);
        }
    }

    @Override
    public void close() {
        this.flushExecutor.shutdown();
        try {
            this.writer.close();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to close sink", ex);
            throw new ProfilerException(ex);
        }
    }

    private void flush() {
        try {
            this.writer.flush();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Unable to flush sink", ex);
        }
    }
}
