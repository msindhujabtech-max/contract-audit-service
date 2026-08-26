package com.contract.audit.service;

import com.contract.audit.dto.AuditRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends email notifications when a Kafka audit event is consumed.
 * Uses Gmail SMTP with an App Password for authentication.
 */
@Slf4j
@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final String notificationEmail;

    public EmailNotificationService(JavaMailSender mailSender,
                                    @Value("${app.audit.notification-email}") String notificationEmail) {
        this.mailSender = mailSender;
        this.notificationEmail = notificationEmail;
    }

    public void sendAuditNotification(AuditRequest request) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("m.sindhujabtech@gmail.com");
            message.setTo(notificationEmail);
            message.setSubject("🛡️ Audit Event: " + request.contractName());
            message.setText(String.format(
                    """
                    Contract Audit Notification
                    ===========================
                    
                    Contract: %s
                    Status:   %s
                    Word Count: %d
                    
                    This event was received via Kafka (async messaging).
                    
                    ---
                    Contract Audit Service
                    """,
                    request.contractName(),
                    request.status(),
                    request.wordCount()
            ));

            mailSender.send(message);
            log.info("Email notification sent to {} for contract '{}'",
                    notificationEmail, request.contractName());
        } catch (Exception e) {
            log.warn("Failed to send email notification: {}", e.getMessage());
        }
    }
}
