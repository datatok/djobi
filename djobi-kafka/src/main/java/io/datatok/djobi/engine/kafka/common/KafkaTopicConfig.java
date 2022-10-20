package io.datatok.djobi.engine.kafka.common;

public class KafkaTopicConfig {
    public String name;
    public boolean create;

    public int partitions = 1;

    public short replicationFactor = 1;
}
