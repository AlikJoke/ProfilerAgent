package ru.joke.profiler.core.output.handlers;

public final class OutputData {

    private String method;
    private String traceId;
    private int depth;
    private long methodElapsedTime;
    private long methodEnterTimestamp;

    public OutputData fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp,
            final String traceId,
            final int depth) {
        this.method = method;
        this.traceId = traceId;
        this.depth = depth;
        this.methodElapsedTime = methodElapsedTime;
        this.methodEnterTimestamp = methodEnterTimestamp;

        return this;
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

    @Override
    public String toString() {
        return "OutputData{"
                + "method='" + method + '\''
                + ", traceId='" + traceId + '\''
                + ", depth=" + depth
                + ", methodElapsedTime=" + methodElapsedTime
                + ", methodEnterTimestamp=" + methodEnterTimestamp
                + '}';
    }
}
