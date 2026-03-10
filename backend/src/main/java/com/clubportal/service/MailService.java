package com.clubportal.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final Environment env;

    @Value("${app.mail.from:}")
    private String from;

    public MailService(JavaMailSender mailSender, Environment env) {
        this.mailSender = mailSender;
        this.env = env;
    }

    @PostConstruct
    public void logMailConfig() {
        log.info("MAIL_CONFIG host={} port={} username={} from={}",
                env.getProperty("spring.mail.host"),
                env.getProperty("spring.mail.port"),
                env.getProperty("spring.mail.username"),
                from
        );
    }

    public void sendPlainText(String to, String subject, String text) {
        if (from == null || from.isBlank()) {
            throw new IllegalStateException("app.mail.from is not configured. Set APP_MAIL_FROM (or MAIL_FROM).");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }
}
