package ru.joke.profiler.output.sinks;

import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;

public final class OutputData {

    private static final int DEFAULT_INITIAL_SPANS_SIZE = 256;
    private static final String NO_SPAN = "-";

    private String method;
    private String traceId;
    private int depth;
    private long methodElapsedTime;
    private long methodEnterTimestamp;
    private String threadName;
    private LocalDateTime timestamp;
    private String spanId;
    private String parentSpanId;

    private Deque<String> spans;
    private long[] spanOverheads;

    public void fill(final OutputData source) {
        fill(
                source.method,
                source.methodElapsedTime,
                source.methodEnterTimestamp,
                source.traceId,
                source.depth,
                source.threadName,
                source.timestamp,
                source.spanId,
                source.parentSpanId
        );
    }

    public void fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp,
            final String traceId,
            final int depth,
            final String spanId,
            final String parentSpanId
    ) {
        fill(
                method,
                methodElapsedTime - pollLastOverhead(),
                methodEnterTimestamp,
                traceId,
                depth,
                Thread.currentThread().getName(),
                LocalDateTime.now(),
                spanId,
                parentSpanId
        );
    }

    private void fill(
            final String method,
            final long methodElapsedTime,
            final long methodEnterTimestamp,
            final String traceId,
            final int depth,
            final String threadName,
            final LocalDateTime timestamp,
            final String spanId,
            final String parentSpanId
    ) {
        this.method = method;
        this.traceId = traceId;
        this.depth = depth;
        this.methodElapsedTime = methodElapsedTime;
        this.methodEnterTimestamp = methodEnterTimestamp;
        this.threadName = threadName;
        this.timestamp = timestamp;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
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

    public void withSpan(final String span) {
        final Deque<String> spans = takeSpans();
        spans.addLast(span);

        final long[] oldOverheads = takeSpanOverheads();
        if (oldOverheads.length < spans.size()) {
            this.spanOverheads = new long[oldOverheads.length * 2];
            System.arraycopy(oldOverheads, 0, this.spanOverheads, 0, oldOverheads.length);
        }
    }

    public String parentSpanId() {
        return this.parentSpanId;
    }

    public String spanId() {
        return this.spanId;
    }

    public String pollLastSpan() {
        return takeSpans().pollLast();
    }

    public String peekLastSpan() {
        return takeSpans().peekLast();
    }

    public void withTraceId(final String traceId) {
        this.traceId = traceId;
    }

    public void increaseOverhead(final long overhead) {
        final int spansCount = this.spans.size() - 1;
        final long[] overheads = takeSpanOverheads();
        for (int i = 0; i < spansCount; i++) {
            overheads[i] += overhead;
        }
    }

    private long pollLastOverhead() {
        final int index = this.spans.size() - 1;
        final long[] spanOverheads = takeSpanOverheads();
        final long result = spanOverheads[index];
        spanOverheads[index] = 0;
        return result;
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

    private Deque<String> takeSpans() {
        if (this.spans == null) {
            this.spans = new ArrayDeque<>(DEFAULT_INITIAL_SPANS_SIZE);
            this.spans.addLast(NO_SPAN);
        }

        return this.spans;
    }

    private long[] takeSpanOverheads() {
        return this.spanOverheads == null ? (this.spanOverheads = new long[DEFAULT_INITIAL_SPANS_SIZE]) : this.spanOverheads;
    }
}
