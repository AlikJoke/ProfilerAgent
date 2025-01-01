package ru.joke.profiler.transformation.spy.jdbc;

import ru.joke.profiler.ProfilerException;
import ru.joke.profiler.configuration.DynamicProfilingConfiguration;
import ru.joke.profiler.configuration.StaticProfilingConfiguration;
import ru.joke.profiler.configuration.meta.ConfigurationParser;
import ru.joke.profiler.output.ExecutionTimeRegistrar;
import ru.joke.profiler.transformation.spy.SpyContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
final class JdbcStatementFactory {

    private static final String DYNAMIC_CONF_SUBSCRIPTION_ID = "jdbc-spy";

    private static JdbcStatementFactory instance;

    private final ExecutionTimeRegistrar registrar;
    private final JdbcSpyConfiguration staticConfiguration;
    private volatile JdbcSpyConfiguration dynamicConfiguration;

    private JdbcStatementFactory(
            final SpyContext context,
            final JdbcSpyConfiguration staticConfiguration,
            final JdbcSpyConfiguration dynamicConfiguration
    ) {
        this.staticConfiguration = staticConfiguration;
        this.dynamicConfiguration = dynamicConfiguration;
        this.registrar = context.registrar();

        context.dynamicConfigurationHolder()
                .subscribeOnChanges(
                        DYNAMIC_CONF_SUBSCRIPTION_ID,
                        (id, c) -> this.dynamicConfiguration = createDynamicSpyConfiguration(c, context.staticConfiguration())
                );
    }

    synchronized static JdbcStatementFactory create(final SpyContext spyContext) {
        if (instance != null) {
            throw new ProfilerException("Singleton factory instance already created: " + instance);
        }

        final StaticProfilingConfiguration staticConfiguration = spyContext.staticConfiguration();
        final JdbcSpyConfiguration staticSpyConfiguration =
                staticConfiguration.dynamicConfigurationEnabled()
                        ? null
                        : createSpyConfiguration(staticConfiguration.minExecutionThresholdNs(), staticConfiguration.spiesProperties());

        final JdbcSpyConfiguration dynamicSpyConfiguration =
                staticSpyConfiguration == null
                        ? createDynamicSpyConfiguration(spyContext.dynamicConfigurationHolder().get(), staticConfiguration)
                        : null;

        return instance = new JdbcStatementFactory(spyContext, staticSpyConfiguration, dynamicSpyConfiguration);
    }

    static Statement createStatement(final Connection connection) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.createStatement();
        }

        return new StatementSpy<>(
                connection.createStatement(),
                instance.registrar,
                configuration
        );
    }

    static Statement createStatement(
            final Connection connection,
            final int resultSetType,
            final int resultSetConcurrency
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.createStatement(resultSetType, resultSetConcurrency);
        }

        return new StatementSpy<>(
                connection.createStatement(resultSetType, resultSetConcurrency),
                instance.registrar,
                configuration
        );
    }

    static Statement createStatement(
            final Connection connection,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        return new StatementSpy<>(
                connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability),
                instance.registrar,
                configuration
        );
    }

    static Statement prepareCall(
            final Connection connection,
            final String query
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareCall(query);
        }

        return new CallableStatementSpy(
                connection.prepareCall(query),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareCall(
            final Connection connection,
            final String query,
            final int resultSetType,
            final int resultSetConcurrency
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareCall(query, resultSetType, resultSetConcurrency);
        }

        return new CallableStatementSpy(
                connection.prepareCall(query, resultSetType, resultSetConcurrency),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareCall(
            final Connection connection,
            final String query,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareCall(query, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        return new CallableStatementSpy(
                connection.prepareCall(query, resultSetType, resultSetConcurrency, resultSetHoldability),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query,
            final int resultSetType,
            final int resultSetConcurrency,
            final int resultSetHoldability
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query, resultSetType, resultSetConcurrency, resultSetHoldability);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query, resultSetType, resultSetConcurrency, resultSetHoldability),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query,
            final int resultSetType,
            final int resultSetConcurrency
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query, resultSetType, resultSetConcurrency);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query, resultSetType, resultSetConcurrency),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query,
            final int[] columnIndexes
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query, columnIndexes);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query, columnIndexes),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query,
            final String[] columnNames
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query, columnNames);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query, columnNames),
                instance.registrar,
                configuration,
                query
        );
    }

    static Statement prepareStatement(
            final Connection connection,
            final String query,
            final int autoGeneratedKeys
    ) throws SQLException {
        final JdbcSpyConfiguration configuration = takeCurrentConfiguration();
        if (configuration.disabled()) {
            return connection.prepareStatement(query, autoGeneratedKeys);
        }

        return new PreparedStatementSpy<>(
                connection.prepareStatement(query, autoGeneratedKeys),
                instance.registrar,
                configuration,
                query
        );
    }

    private static JdbcSpyConfiguration createDynamicSpyConfiguration(
            final DynamicProfilingConfiguration dynamicConfiguration,
            final StaticProfilingConfiguration staticConfiguration
    ) {
        final long minExecutionThresholdNs = Math.max(dynamicConfiguration.minExecutionThresholdNs(), staticConfiguration.minExecutionThresholdNs());
        return createSpyConfiguration(minExecutionThresholdNs, dynamicConfiguration.spiesProperties());
    }

    private static JdbcSpyConfiguration takeCurrentConfiguration() {
        return instance.staticConfiguration == null
                ? instance.dynamicConfiguration
                : instance.staticConfiguration;
    }

    private static JdbcSpyConfiguration createSpyConfiguration(
            final long globalMinExecutionThresholdNs,
            final Map<String, String> spiesProperties
    ) {
        final Map<String, String> finalSpiesProperties = new HashMap<>(spiesProperties);
        finalSpiesProperties.put(JdbcSpyConfiguration.MIN_EXECUTION_THRESHOLD, String.valueOf(globalMinExecutionThresholdNs));

        return ConfigurationParser.parse(JdbcSpyConfiguration.class, finalSpiesProperties);
    }
}
