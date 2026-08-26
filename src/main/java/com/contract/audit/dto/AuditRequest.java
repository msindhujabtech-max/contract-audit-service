package com.contract.audit.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuditRequest(
        String contractName,
        String status,
        int wordCount,
        String question,
        String answer
) {}
