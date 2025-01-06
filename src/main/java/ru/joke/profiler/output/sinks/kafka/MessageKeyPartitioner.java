package ru.joke.profiler.output.sinks.kafka;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;

public final class MessageKeyPartitioner implements Partitioner {

    @Override
    public int partition(
            final String topic,
            final Object key,
            final byte[] keyBytes,
            final Object value,
            final byte[] valueBytes,
            final Cluster cluster
    ) {
        final List<PartitionInfo> partitions = cluster.partitionsForTopic(topic);
        final int numPartitions = partitions.size();
        final int messageHash = key.hashCode();
        return Math.abs(messageHash) % numPartitions;
    }

    @Override
    public void close() {
    }

    @Override
    public void configure(final Map<String, ?> configs) {
    }
}