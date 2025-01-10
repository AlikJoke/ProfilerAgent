package ru.joke.profiler.output.sinks.fs.stream.file;

import ru.joke.profiler.util.ProfilerThreadFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

final class RotatableBufferedWriter extends Writer {

    private static final String ROTATING_THREAD_NAME = "profiler-rotating-thread";

    private final FileSinkConfiguration configuration;
    private final AtomicLong overflowCounter;
    private final Rotator rotator;
    private final ExecutorService rotationExecutor;
    private final Semaphore rotationSemaphore;
    private volatile Writer writer;

    RotatableBufferedWriter(
            final Writer writer,
            final FileSinkConfiguration configuration
    ) {
        this.writer = new BufferedWriter(writer, configuration.bufferSize());
        this.configuration = configuration;
        this.overflowCounter = new AtomicLong(findCurrentFileSize(configuration));
        this.rotator = Rotator.getInstance();
        if (configuration.rotation().mode() == FileSinkConfiguration.Rotation.RotationMode.ASYNC) {
            this.rotationSemaphore = new Semaphore(1);
            this.rotationExecutor = Executors.newSingleThreadExecutor(new ProfilerThreadFactory(ROTATING_THREAD_NAME, false));
        } else {
            this.rotationSemaphore = null;
            this.rotationExecutor = null;
        }
    }

    @Override
    public void write(char[] cbuf, int off, int len) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void write(final String str) throws IOException {
        while (!tryWrite(str));
        rotateIfNeed(str);
    }

    @Override
    public void flush() throws IOException {
        while (!tryFlush());
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.rotationExecutor != null) {
            this.rotationExecutor.shutdownNow();
        }

        this.writer.close();
        this.writer = null;
    }

    private boolean tryFlush() throws IOException {
        final Writer writer = this.writer;
        if (writer == null) {
            return false;
        }

        try {
            writer.flush();
            return true;
        } catch (IOException ex) {
            if (this.writer == writer) {
                handleException(writer, ex);
            }

            return false;
        }
    }

    private void rotateIfNeed(final String str) throws IOException {
        if (this.overflowCounter.addAndGet(str.length()) < this.configuration.rotation().overflowLimitBytes()) {
            return;
        }

        if (this.rotationExecutor == null) {
            rotate();
            return;
        }

        if (this.rotationSemaphore.tryAcquire()) {
            this.rotationExecutor.submit(this::rotate);
        }
    }

    private boolean tryWrite(final String str) throws IOException {
        final Writer writer = this.writer;
        if (writer == null) {
            return false;
        }

        try {
            writer.write(str);
            return true;
        } catch (IOException ex) {
            if (this.writer == writer) {
                handleException(writer, ex);
            }

            return false;
        }
    }

    private synchronized void handleException(
            final Writer writer,
            final IOException ex
    ) throws IOException {
        if (this.writer == writer) {
            throw ex;
        }
    }

    private synchronized boolean rotate() throws IOException {
        if (this.writer == null) {
            return false;
        }

        try {
            final long overflowLimit = this.configuration.rotation().overflowLimitBytes();

            long overflowCounter;
            do {
                overflowCounter = this.overflowCounter.get();
                if (overflowCounter < overflowLimit) {
                    return false;
                }

                final Writer prevWriter = this.writer;
                prevWriter.close();

                final File targetFile = this.rotator.rotate(this.configuration.filePath(), this.configuration.rotation().maxRotations());

                this.writer = new BufferedWriter(
                        ExistingFilePolicy.APPEND.createWriter(targetFile.getAbsolutePath()),
                        this.configuration.bufferSize()
                );
            } while (this.overflowCounter.addAndGet(-overflowCounter) >= overflowLimit);

            return true;
        } finally {
            if (this.rotationSemaphore != null) {
                this.rotationSemaphore.release();
            }
        }
    }

    private long findCurrentFileSize(final FileSinkConfiguration configuration) {
        final File file = new File(configuration.filePath());
        return file.length();
    }
}
