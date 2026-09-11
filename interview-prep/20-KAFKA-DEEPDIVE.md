# Kafka — Deep Dive

You built with Kafka, so expect deeper probing than the basics in 04-MICROSERVICES.

## Architecture Recap

```
Producer → [ Topic (split into Partitions) ] → Consumer Group
                     stored on Brokers
              coordinated by Zookeeper/KRaft
```

## Core Concepts Deep Dive

### Topics & Partitions
A **topic** is split into **partitions** for parallelism and scale. Each partition is an ordered, immutable log.
- Ordering is guaranteed WITHIN a partition, NOT across partitions.
- More partitions = more parallelism (more consumers can read simultaneously).

```
Topic "contract-audit-topic"
├── Partition 0: [msg0][msg3][msg6]...
├── Partition 1: [msg1][msg4][msg7]...
└── Partition 2: [msg2][msg5][msg8]...
```

### Partitioning strategy (how a message picks a partition)
- **With a key:** `hash(key) % numPartitions` — same key always goes to the same partition (preserves order per key).
- **Without a key:** round-robin across partitions.

```java
// Keyed send — all events for one contract stay ordered in the same partition
kafkaTemplate.send("contract-audit-topic", contractName, message);
//                                          ^key
```
> In your project, using `contractName` as the key means all audit events for a given contract keep their order.

### Consumer Groups & Rebalancing
- A **consumer group** shares the work — each partition is consumed by exactly ONE consumer in the group.
- If you have 3 partitions and 3 consumers → one partition each (max parallelism).
- More consumers than partitions → extras sit idle.
- **Rebalancing** — when a consumer joins/leaves, Kafka reassigns partitions. During rebalance, consumption briefly pauses.

```
3 partitions, group "audit-service-group":
Consumer A → Partition 0
Consumer B → Partition 1
Consumer C → Partition 2
```

### Offsets
The **offset** is a consumer's position in a partition (which messages it has read).
- **Auto-commit** — Kafka periodically commits offsets (simple, risk of reprocessing/loss).
- **Manual commit** — you commit after successfully processing (safer, at-least-once).
- `auto-offset-reset`: `earliest` (read from start) or `latest` (only new messages). You set `earliest`.

## Delivery Semantics

| Guarantee | Meaning | How |
|-----------|---------|-----|
| At-most-once | May lose, no duplicates | Commit offset before processing |
| At-least-once | No loss, may duplicate | Commit offset after processing (default) |
| Exactly-once | No loss, no duplicates | Idempotent producer + transactions |

### Making consumers idempotent (handle duplicates)
Since at-least-once can deliver twice, design consumers to be idempotent:
```java
@KafkaListener(topics = "contract-audit-topic", groupId = "audit-service-group")
public void consume(String message) {
    String eventId = extractId(message);
    if (processedIds.contains(eventId)) return;  // skip duplicate
    process(message);
    processedIds.add(eventId);
}
```

## Reliability Features

### Replication
Each partition is replicated across brokers for fault tolerance.
- **Leader** — handles reads/writes for a partition.
- **Followers** — replicate the leader; one is promoted if the leader dies.
- **ISR (In-Sync Replicas)** — replicas caught up with the leader.
- **acks** producer setting:
  - `acks=0` — fire and forget (fastest, may lose)
  - `acks=1` — leader confirms (balanced)
  - `acks=all` — all ISR confirm (safest, slowest)

### Dead Letter Queue (DLQ)
Messages that repeatedly fail processing are routed to a separate DLQ topic for later inspection, so they don't block the main flow.

## Kafka vs Traditional Queue (why log-based matters)
- Messages are RETAINED (by time/size), not deleted on consume → consumers can replay history.
- Multiple consumer groups read the SAME topic independently (one for audit, another for analytics).
- Scales horizontally via partitions.

## Zookeeper vs KRaft
- **Zookeeper** — older coordination service for brokers (what your compose uses).
- **KRaft** — newer, removes the Zookeeper dependency (Kafka manages its own metadata). Simpler ops, the future direction.

## Spring Kafka Specifics (your implementation)

```java
// Producer config
spring.kafka.producer.key-serializer=...StringSerializer
spring.kafka.producer.value-serializer=...StringSerializer

// Consumer config
spring.kafka.consumer.group-id=audit-service-group
spring.kafka.consumer.auto-offset-reset=earliest

// Producer
kafkaTemplate.send(topic, key, message);

// Consumer
@KafkaListener(topics = "contract-audit-topic", groupId = "audit-service-group")
public void consume(String message) { ... }
```

## Common Kafka Interview Questions

**Q: How does Kafka guarantee ordering?**
> Ordering is guaranteed only within a partition. To keep related messages ordered, give them the same key so they hash to the same partition (e.g., all events for one contract).

**Q: What happens when a consumer in a group dies?**
> Kafka triggers a rebalance — its partitions are reassigned to the remaining consumers in the group. Consumption briefly pauses during the rebalance, then resumes.

**Q: How do you achieve exactly-once processing?**
> Use idempotent producers + Kafka transactions, or make consumers idempotent (dedupe by an event ID). At-least-once + idempotent consumer is the common pragmatic approach.

**Q: Kafka vs RabbitMQ?**
> Kafka is a log-based streaming platform — messages persist and can be replayed, high throughput via partitions, pull-based. RabbitMQ is a traditional broker — messages deleted after consume, rich routing, push-based. Kafka for event streaming/high volume; RabbitMQ for task queues/complex routing.

**Q: What is consumer lag?**
> The gap between the latest offset in a partition and the consumer's committed offset — i.e., how far behind the consumer is. High lag means consumers can't keep up; scale consumers or partitions.

**Q: Why did you use Kafka in your project instead of a direct HTTP call?**
> For chat audit events I don't need an immediate response. Kafka decouples the analyser from the audit service — if the audit consumer is down, messages persist and are processed on recovery, so no events are lost. HTTP would fail immediately if the service were down.
