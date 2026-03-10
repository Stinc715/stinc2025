package com.clubportal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class VerificationEmailSenderService {

    private static final Logger log = LoggerFactory.getLogger(VerificationEmailSenderService.class);
    public static final String REASON_SMTP_AUTH_FAILED = "SMTP_AUTH_FAILED";
    public static final String REASON_RECIPIENT_REJECTED = "RECIPIENT_REJECTED";
    @Deprecated
    public static final String REASON_RECIPIENT_NOT_VERIFIED = REASON_RECIPIENT_REJECTED;
    public static final String REASON_SEND_FAILED = "SEND_FAILED";

    private final MailService mailService;

    public VerificationEmailSenderService(MailService mailService) {
        this.mailService = mailService;
    }

    public SendEmailResult sendRegistrationCode(String toEmail, String code, Instant expiresAt) {
        long minutes = Math.max(1, Duration.between(Instant.now(), expiresAt).toMinutes());
        String text = """
                Your verification code is %s.

                This code expires in %d minute(s).
                If you did not request this code, you can ignore this email.
                """.formatted(code, minutes);
        return sendEmail(
                toEmail,
                "Club Booking Portal verification code",
                text,
                "registration verification"
        );
    }

    public SendEmailResult sendPasswordResetLink(String toEmail, String resetLink, Instant expiresAt) {
        long minutes = Math.max(1, Duration.between(Instant.now(), expiresAt).toMinutes());
        String text = """
                We received a request to reset your Club Booking Portal password.

                Open the link below to set a new password:
                %s

                This link expires in %d minute(s).
                If you did not request a password reset, you can ignore this email.
                """.formatted(resetLink, minutes);
        return sendEmail(
                toEmail,
                "Club Booking Portal password reset",
                text,
                "password reset"
        );
    }

    private SendEmailResult sendEmail(String toEmail, String subject, String text, String context) {
        try {
            mailService.sendPlainText(toEmail, subject, text);
            return SendEmailResult.ok();
        } catch (MailAuthenticationException ex) {
            log.error("SMTP auth failed when sending {} email to {}", context, toEmail, ex);
            return SendEmailResult.failed(
                    REASON_SMTP_AUTH_FAILED,
                    "SMTP authentication failed. Please verify SPRING_MAIL_USERNAME / SPRING_MAIL_PASSWORD."
            );
        } catch (MailSendException ex) {
            log.error("Failed to send {} email to {}", context, toEmail, ex);
            String message = ex.getMessage();
            if (isRecipientRejected(message)) {
                return SendEmailResult.failed(
                        REASON_RECIPIENT_REJECTED,
                        "Recipient email address was rejected by SMTP provider."
                );
            }
            return SendEmailResult.failed(
                    REASON_SEND_FAILED,
                    "Email service unavailable. Please try again later."
            );
        } catch (Exception ex) {
            log.error("Failed to send {} email to {}", context, toEmail, ex);
            return SendEmailResult.failed(
                    REASON_SEND_FAILED,
                    "Email service unavailable. Please try again later."
            );
        }
    }

    public record SendEmailResult(boolean success, String reasonCode, String message) {
        public static SendEmailResult ok() {
            return new SendEmailResult(true, null, null);
        }

        public static SendEmailResult failed(String reasonCode, String message) {
            return new SendEmailResult(false, reasonCode, message);
        }
    }

    private static boolean isRecipientRejected(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.toLowerCase();
        return lower.contains("email address is not verified")
                || lower.contains("recipient address rejected")
                || lower.contains("invalid address")
                || lower.contains("mailbox unavailable")
                || (lower.contains("550") && lower.contains("recipient"));
    }
}
