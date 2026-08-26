package com.contract.audit.controller;

import com.contract.audit.dto.AuditRequest;
import com.contract.audit.kafka.AuditKafkaConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final List<AuditRequest> auditLog = new CopyOnWriteArrayList<>();
    private final AuditKafkaConsumer kafkaConsumer;

    public AuditController(AuditKafkaConsumer kafkaConsumer) {
        this.kafkaConsumer = kafkaConsumer;
    }

    /**
     * POST /api/audit/log — receives audit events via HTTP (WebClient from analyser).
     * Used for: file upload events (synchronous communication).
     */
    @PostMapping("/log")
    public Mono<String> logAudit(@RequestBody AuditRequest request) {
        log.info("HTTP audit received -> contract: '{}', status: '{}', wordCount: {}",
                request.contractName(), request.status(), request.wordCount());

        auditLog.add(request);

        return Mono.just("Audit logged successfully. Total entries: " + auditLog.size());
    }

    /**
     * GET /api/audit/logs — view all audit entries from both HTTP and Kafka.
     * Open this in your browser to see the results.
     */
    @GetMapping("/logs")
    public Mono<Map<String, Object>> getAllLogs() {
        return Mono.just(Map.of(
                "httpAuditLogs", auditLog,
                "kafkaAuditLogs", kafkaConsumer.getKafkaAuditLog(),
                "totalHttp", auditLog.size(),
                "totalKafka", kafkaConsumer.getKafkaAuditLog().size()
        ));
    }
}
