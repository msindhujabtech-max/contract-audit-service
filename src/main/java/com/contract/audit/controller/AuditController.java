package com.contract.audit.controller;

import com.contract.audit.dto.AuditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@RestController
@RequestMapping("/api/audit")
public class AuditController {

    private final List<AuditRequest> auditLog = new CopyOnWriteArrayList<>();

    @PostMapping("/log")
    public Mono<String> logAudit(@RequestBody AuditRequest request) {
        log.info("Audit received -> contract: '{}', status: '{}', wordCount: {}",
                request.contractName(), request.status(), request.wordCount());

        auditLog.add(request);

        return Mono.just("Audit logged successfully. Total entries: " + auditLog.size());
    }
}
