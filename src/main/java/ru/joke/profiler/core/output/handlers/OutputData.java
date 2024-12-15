package ru.joke.profiler.core.output.handlers;

import java.time.LocalDateTime;

public final class OutputData {

    private String method;
    private String traceId;
    private int depth;
    private long methodElapsedTime;
    private long methodEnterTimestamp;
    private String threadName;
    private LocalDateTime timestamp;

    public void fill(final OutputData source) {
        fill(
                source.method,
                source.methodElapsedTime,
                source.methodEnterTimestamp,
                source.traceId,
                source.depth,
                source.threadName,
                source.timestamp
        );
    }

    public void fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp) {
        fill(method, methodElapsedTime, methodEnterTimestamp, null, 0);
    }

    public void fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp,
            final String traceId,
            final int depth) {
        fill(method, methodElapsedTime, methodEnterTimestamp, traceId, depth, Thread.currentThread().getName(), LocalDateTime.now());
    }

    private void fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp,
            final String traceId,
            final int depth,
            final String threadName,
            final LocalDateTime timestamp) {
        this.method = method;
        this.traceId = traceId;
        this.depth = depth;
        this.methodElapsedTime = methodElapsedTime;
        this.methodEnterTimestamp = methodEnterTimestamp;
        this.threadName = threadName;
        this.timestamp = timestamp;
    }

    public String method() {
        return this.method;
    }

    public String traceId() {
        return this.traceId;
    }

    public int depth() {
        return this.depth;
    }

    public long methodEnterTimestamp() {
        return this.methodEnterTimestamp;
    }

    public long methodElapsedTime() {
        return this.methodElapsedTime;
    }

    public String thread() {
        return this.threadName;
    }

    public LocalDateTime timestamp() {
        return this.timestamp;
    }

    public void withDepth(int depth) {
        this.depth = depth;
    }

    public void withTraceId(final String traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return "OutputData{"
                + "method='" + method + '\''
                + ", traceId='" + traceId + '\''
                + ", depth=" + depth
                + ", methodElapsedTime=" + methodElapsedTime
                + ", methodEnterTimestamp=" + methodEnterTimestamp
                + ", thread=" + threadName + '\''
                + ", timestamp=" + timestamp
                + '}';
    }
}
