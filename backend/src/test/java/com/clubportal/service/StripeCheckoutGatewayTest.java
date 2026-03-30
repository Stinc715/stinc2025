package com.clubportal.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StripeCheckoutGatewayTest {

    @Test
    void verifiesWebhookWithValidSignatureAndMetadataFallback() {
        StripeCheckoutGateway gateway = new StripeCheckoutGateway();
        ReflectionTestUtils.setField(gateway, "stripeSecretKey", "sk_test_123");
        ReflectionTestUtils.setField(gateway, "stripeWebhookSecret", "whsec_test_123");

        String payload = """
                {
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_123",
                      "payment_status": "paid",
                      "metadata": {
                        "club_portal_session_id": "checkout_internal_123"
                      }
                    }
                  }
                }
                """;
        String timestamp = "1710000000";
        String header = "t=" + timestamp + ",v1=" + sign("whsec_test_123", timestamp + "." + payload);

        StripeCheckoutGateway.VerifiedWebhookEvent event = gateway.verifyWebhook(payload, header);

        assertTrue(gateway.isConfigured());
        assertTrue(gateway.isWebhookConfigured());
        assertEquals("checkout.session.completed", event.type());
        assertEquals("cs_test_123", event.providerSessionId());
        assertEquals("checkout_internal_123", event.internalSessionId());
        assertEquals("paid", event.paymentStatus());
    }

    @Test
    void rejectsWebhookWithInvalidSignature() {
        StripeCheckoutGateway gateway = new StripeCheckoutGateway();
        ReflectionTestUtils.setField(gateway, "stripeSecretKey", "sk_test_123");
        ReflectionTestUtils.setField(gateway, "stripeWebhookSecret", "whsec_test_123");

        String payload = """
                {
                  "type": "checkout.session.completed",
                  "data": {
                    "object": {
                      "id": "cs_test_123",
                      "client_reference_id": "checkout_internal_123",
                      "payment_status": "paid"
                    }
                  }
                }
                """;

        assertThrows(IllegalArgumentException.class,
                () -> gateway.verifyWebhook(payload, "t=1710000000,v1=bad-signature"));
    }

    private static String sign(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(raw.length * 2);
            for (byte b : raw) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new AssertionError(ex);
        }
    }
}
