package com.contract.audit.kafka;

import com.contract.audit.dto.AuditRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Kafka Consumer — listens to "contract-audit-topic" for async audit events.
 *
 * This is the ASYNC counterpart to the REST endpoint (AuditController).
 *
 * Comparison:
 * ┌────────────────────────────────────────────────────────────────┐
 * │  HTTP (AuditController)         │  Kafka (AuditKafkaConsumer)  │
 * ├─────────────────────────────────┼──────────────────────────────┤
 * │  Triggered by: file upload      │  Triggered by: chat question │
 * │  Communication: synchronous     │  Communication: asynchronous │
 * │  Caller waits for response      │  Caller doesn't wait         │
 * │  Fails if this service is down  │  Message waits in Kafka      │
 * │  1-to-1 (HTTP request/response) │  1-to-many (pub/sub pattern) │
 * └─────────────────────────────────┴──────────────────────────────┘
 */
@Slf4j
@Component
public class AuditKafkaConsumer {

    private final List<AuditRequest> kafkaAuditLog = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "contract-audit-topic", groupId = "audit-service-group")
    public void consumeAuditEvent(String message) {
        try {
            AuditRequest request = objectMapper.readValue(message, AuditRequest.class);

            log.info("Kafka audit received -> contract: '{}', status: '{}', wordCount: {}",
                    request.contractName(), request.status(), request.wordCount());

            kafkaAuditLog.add(request);

            log.info("Kafka audit log size: {}", kafkaAuditLog.size());
        } catch (Exception e) {
            log.error("Failed to process Kafka audit message: {}", e.getMessage());
        }
    }

    public List<AuditRequest> getKafkaAuditLog() {
        return kafkaAuditLog;
    }
}
