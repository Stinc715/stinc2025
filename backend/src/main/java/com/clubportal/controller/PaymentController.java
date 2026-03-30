package com.clubportal.controller;

import com.clubportal.dto.CheckoutSessionCreateRequest;
import com.clubportal.service.CheckoutSessionService;
import com.clubportal.service.CurrentUserService;
import com.clubportal.service.PaymentException;
import com.clubportal.service.StripeCheckoutGateway;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final CheckoutSessionService checkoutSessionService;
    private final CurrentUserService currentUserService;
    private final StripeCheckoutGateway stripeCheckoutGateway;

    public PaymentController(CheckoutSessionService checkoutSessionService,
                             CurrentUserService currentUserService,
                             StripeCheckoutGateway stripeCheckoutGateway) {
        this.checkoutSessionService = checkoutSessionService;
        this.currentUserService = currentUserService;
        this.stripeCheckoutGateway = stripeCheckoutGateway;
    }

    @PostMapping("/checkout-sessions")
    public ResponseEntity<?> createCheckoutSession(@RequestBody CheckoutSessionCreateRequest request) {
        try {
            return ResponseEntity.ok(checkoutSessionService.createCheckoutSession(currentUserService.requireUser(), request));
        } catch (PaymentException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    @GetMapping("/checkout-sessions/{sessionId}")
    public ResponseEntity<?> getCheckoutSession(@PathVariable String sessionId) {
        try {
            return ResponseEntity.ok(checkoutSessionService.getCheckoutSession(currentUserService.requireUser(), sessionId));
        } catch (PaymentException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    @PostMapping("/checkout-sessions/{sessionId}/cancel")
    public ResponseEntity<?> cancelCheckoutSession(@PathVariable String sessionId) {
        try {
            return ResponseEntity.ok(checkoutSessionService.cancelCheckoutSession(currentUserService.requireUser(), sessionId));
        } catch (PaymentException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    @PostMapping("/checkout-sessions/{sessionId}/confirm-virtual")
    public ResponseEntity<?> confirmVirtualCheckout(@PathVariable String sessionId) {
        try {
            return ResponseEntity.ok(checkoutSessionService.confirmVirtualCheckout(currentUserService.requireUser(), sessionId));
        } catch (PaymentException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }

    @PostMapping("/webhook/stripe")
    public ResponseEntity<?> handleStripeWebhook(@RequestBody String payload,
                                                 @RequestHeader(name = "Stripe-Signature", required = false) String signatureHeader) {
        try {
            StripeCheckoutGateway.VerifiedWebhookEvent event = stripeCheckoutGateway.verifyWebhook(payload, signatureHeader);
            checkoutSessionService.handleStripeWebhook(event);
            return ResponseEntity.ok(Map.of("received", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ex.getMessage());
        } catch (PaymentException ex) {
            return ResponseEntity.status(ex.getStatus()).body(ex.getMessage());
        }
    }
}
