package ru.joke.profiler.output.handlers.kafka;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import ru.joke.profiler.output.handlers.OutputDataSink;

import java.time.Duration;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

final class KafkaClusterValidator {

    private static final Logger logger = Logger.getLogger(OutputDataSink.class.getCanonicalName());

    private static final int DEFAULT_ADMIN_REQUEST_TIMEOUT_MS = 5_000;

    boolean isValid(final Map<String, String> producerProperties) {
        return isKafkaClusterAlive(producerProperties);
    }

    private boolean isKafkaClusterAlive(final Map<String, String> properties) {
        final AdminClient adminClient = createAdminClient(properties);
        try {
            final DescribeClusterOptions describeClusterOptions =
                    new DescribeClusterOptions()
                            .includeAuthorizedOperations(false)
                            .timeoutMs(DEFAULT_ADMIN_REQUEST_TIMEOUT_MS);

            final KafkaFuture<Collection<Node>> nodesFuture = adminClient.describeCluster(describeClusterOptions).nodes();
            final Collection<Node> nodes = nodesFuture.get();
            return nodes != null && !nodes.isEmpty();
        } catch (ExecutionException e) {
            logger.log(Level.WARNING, "Unable to validate cluster state", e);
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            tryClose(adminClient);
        }
    }

    private void tryClose(final AdminClient adminClient) {
        try {
            adminClient.close(Duration.ofMillis(DEFAULT_ADMIN_REQUEST_TIMEOUT_MS));
        } catch (org.apache.kafka.common.errors.TimeoutException ex) {
            logger.log(Level.WARNING, "Unable to close kafka admin client", ex);
        }
    }

    private AdminClient createAdminClient(final Map<String, String> producerProperties) {
        final Map<String, Object> adminClientProperties = createAdminClientProperties(producerProperties);
        return AdminClient.create(adminClientProperties);
    }

    private Map<String, Object> createAdminClientProperties(final Map<String, String> properties) {
        final Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, properties.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, DEFAULT_ADMIN_REQUEST_TIMEOUT_MS);

        return props;
    }
}
