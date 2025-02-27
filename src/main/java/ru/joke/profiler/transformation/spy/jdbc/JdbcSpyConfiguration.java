package ru.joke.profiler.transformation.spy.jdbc;

import ru.joke.profiler.configuration.meta.ProfilerConfigurationPropertiesWrapper;
import ru.joke.profiler.configuration.meta.ProfilerConfigurationProperty;
import ru.joke.profiler.configuration.util.NanoTimePropertyParser;

import static ru.joke.profiler.transformation.spy.jdbc.JdbcSpyInjectorFactory.SPY_ID;
import static ru.joke.profiler.util.ArgUtil.checkNonNegative;
import static ru.joke.profiler.util.ArgUtil.checkNotEmpty;

final class JdbcSpyConfiguration {

    static final String PREFIX = SPY_ID + ".";
    static final String DISABLED = "disabled";
    static final String MAX_PRINTED_CHARACTERS = "max_printed_query_characters";
    static final String MAX_BATCH_QUERIES_TO_PRINT = "max_printed_batch_queries";
    static final String BATCH_QUERIES_DELIMITER = "batch_queries_delimiter";
    static final String QUERY_PARAMETERS_DELIMITER = "query_parameters_delimiter";
    static final String PRINT_QUERY_PARAMETERS = "print_query_parameters";
    static final String MIN_EXECUTION_THRESHOLD = "query_min_execution_threshold";

    private final boolean disabled;
    private final boolean printQueryParameters;
    private final int maxPrintedQueryCharacters;
    private final int maxBatchQueriesToPrint;
    private final String batchQueriesDelimiter;
    private final long minExecutionThresholdNs;
    private final String queryParametersDelimiter;

    @ProfilerConfigurationPropertiesWrapper(prefix = PREFIX)
    JdbcSpyConfiguration(
            @ProfilerConfigurationProperty(name = DISABLED) final boolean disabled,
            @ProfilerConfigurationProperty(name = PRINT_QUERY_PARAMETERS, defaultValue = "true") final boolean printQueryParameters,
            @ProfilerConfigurationProperty(name = MAX_PRINTED_CHARACTERS, defaultValue = "-1") final int maxPrintedQueryCharacters,
            @ProfilerConfigurationProperty(name = MAX_BATCH_QUERIES_TO_PRINT, defaultValue = "-1") final int maxBatchQueriesToPrint,
            @ProfilerConfigurationProperty(name = BATCH_QUERIES_DELIMITER, defaultValue = ";") final String batchQueriesDelimiter,
            @ProfilerConfigurationProperty(name = QUERY_PARAMETERS_DELIMITER, defaultValue = ",") final String queryParametersDelimiter,
            @ProfilerConfigurationProperty(name = MIN_EXECUTION_THRESHOLD, defaultValue = "0", parser = NanoTimePropertyParser.class) final long minExecutionThresholdNs
    ) {
        this.disabled = disabled;
        this.maxPrintedQueryCharacters =
                maxPrintedQueryCharacters == -1
                        ? Integer.MAX_VALUE
                        : checkNonNegative(maxPrintedQueryCharacters, "maxPrintedQueryCharacters");
        this.maxBatchQueriesToPrint =
                maxBatchQueriesToPrint == -1
                        ? Integer.MAX_VALUE
                        : checkNonNegative(maxBatchQueriesToPrint, "maxBatchQueriesToPrint");
        this.batchQueriesDelimiter = checkNotEmpty(batchQueriesDelimiter, "batchQueriesDelimiter");
        this.queryParametersDelimiter = checkNotEmpty(queryParametersDelimiter, "queryParametersDelimiter");
        this.minExecutionThresholdNs = checkNonNegative(minExecutionThresholdNs, "minExecutionThresholdNs");
        this.printQueryParameters = printQueryParameters;
    }

    boolean disabled() {
        return disabled;
    }

    int maxPrintedQueryCharacters() {
        return maxPrintedQueryCharacters;
    }

    int maxBatchQueriesToPrint() {
        return maxBatchQueriesToPrint;
    }

    String batchQueriesDelimiter() {
        return batchQueriesDelimiter;
    }

    String queryParametersDelimiter() {
        return queryParametersDelimiter;
    }

    boolean printQueryParameters() {
        return printQueryParameters;
    }

    long minExecutionThresholdNs() {
        return minExecutionThresholdNs;
    }

    @Override
    public String toString() {
        return "JdbcSpyConfiguration{"
                + "disabled=" + disabled
                + ", printQueryParameters=" + printQueryParameters
                + ", maxPrintedQueryCharacters=" + maxPrintedQueryCharacters
                + ", maxBatchQueriesToPrint=" + maxBatchQueriesToPrint
                + ", batchQueriesDelimiter='" + batchQueriesDelimiter + '\''
                + ", queryParametersDelimiter='" + queryParametersDelimiter + '\''
                + ", minExecutionThresholdNs=" + minExecutionThresholdNs
                + '}';
    }
}
