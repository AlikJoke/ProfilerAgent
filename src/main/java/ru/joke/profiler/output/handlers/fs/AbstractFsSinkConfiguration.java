package ru.joke.profiler.output.handlers.fs;

public abstract class AbstractFsSinkConfiguration {

    protected static final String OUTPUT_DATA_PATTERN = "output_data_pattern";

    protected final String outputDataPattern;

    protected AbstractFsSinkConfiguration(final String outputDataPattern) {
        this.outputDataPattern = outputDataPattern;
    }

    public String outputDataPattern() {
        return outputDataPattern;
    }
}
