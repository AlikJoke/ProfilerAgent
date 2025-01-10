package ru.joke.profiler.output.sinks.util.injectors;

public final class OutputStringDataFormatterFactory {

    private static final String DEFAULT_FORMAT = "${current_ts} [${source},${trace_id},${parent_span_id},${span_id},${depth}] --- [${thread}] --- ${method} : ${enter_ts} : ${elapsed}";

    public static OutputStringDataFormatter create(final String pattern) {
        return new OutputStringDataFormatter(
                pattern == null || pattern.isEmpty()
                        ? DEFAULT_FORMAT
                        : pattern
        );
    }
}
