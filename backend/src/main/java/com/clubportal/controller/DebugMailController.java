package com.clubportal.controller;

import com.clubportal.service.MailService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("dev")
@RestController
@RequestMapping("/api/debug")
public class DebugMailController {

    private final MailService mailService;

    public DebugMailController(MailService mailService) {
        this.mailService = mailService;
    }

    // GET /api/debug/send-test-email?to=you@example.com
    @GetMapping("/send-test-email")
    public ResponseEntity<String> sendTestEmail(@RequestParam("to") String to) {
        try {
            mailService.sendPlainText(
                    to,
                    "SMTP test email - Club Enrolment Portal",
                    "This is a test email sent via the configured SMTP provider. If you received it, SMTP is working."
            );
            return ResponseEntity.ok("sent");
        } catch (Exception ex) {
            String message = ex.getClass().getSimpleName() + ": " + ex.getMessage();
            return ResponseEntity.internalServerError().body(message);
        }
    }
}
