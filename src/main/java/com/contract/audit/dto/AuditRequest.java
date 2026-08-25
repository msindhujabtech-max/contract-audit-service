package com.contract.audit.dto;

public record AuditRequest(
        String contractName,
        String status,
        int wordCount
) {}
