package com.cpf.notification.email;

import com.cpf.notification.spi.CpfNotificationProvider;
import com.cpf.notification.api.CpfNotificationProviderStatus;
import com.cpf.notification.api.CpfNotificationRequest;
import com.cpf.notification.api.CpfNotificationResult;
import jakarta.mail.MessagingException;
import java.util.Objects;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

/** SMTP adapter behind the CPF Notification Provider SPI. */
public final class CpfEmailNotificationProvider implements CpfNotificationProvider {
    private final JavaMailSender sender;
    private final String from;

    public CpfEmailNotificationProvider(JavaMailSender sender, String from) {
        this.sender = Objects.requireNonNull(sender, "sender");
        if (from == null || from.isBlank()) {
            throw new IllegalArgumentException("notification email from address is required");
        }
        this.from = from.trim();
    }

    @Override
    public String channel() {
        return "EMAIL";
    }

    @Override
    public CpfNotificationResult send(CpfNotificationRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(request.recipient());
        message.setSubject(request.variables().getOrDefault("subject", request.templateId()));
        message.setText(request.variables().getOrDefault("body", ""));
        try {
            sender.send(message);
            return CpfNotificationResult.sent(request.notificationId(), "SMTP", null);
        } catch (MailException exception) {
            // SMTP acceptance can be unknown after a transport interruption. Never blind-retry here.
            return CpfNotificationResult.unknown(
                    request.notificationId(), "SMTP",
                    "SMTP_ACCEPTANCE_UNKNOWN:" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public CpfNotificationProviderStatus health() {
        if (!(sender instanceof JavaMailSenderImpl implementation)) {
            return CpfNotificationProviderStatus.unknown("SMTP_HEALTH_UNSUPPORTED");
        }
        try {
            implementation.testConnection();
            return CpfNotificationProviderStatus.up();
        } catch (MessagingException | RuntimeException exception) {
            return CpfNotificationProviderStatus.down("SMTP_UNAVAILABLE");
        }
    }
}
