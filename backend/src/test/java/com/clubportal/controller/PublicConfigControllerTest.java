package com.clubportal.controller;

import org.junit.jupiter.api.Test;
import com.clubportal.service.CheckoutSessionService;
import com.clubportal.security.StreamAuthCookieService;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PublicConfigControllerTest {

    @Test
    void returnsTrimmedPublicConfigWithoutLeakingSecrets() {
        CheckoutSessionService checkoutSessionService = mock(CheckoutSessionService.class);
        when(checkoutSessionService.paymentsEnabled()).thenReturn(true);
        when(checkoutSessionService.paymentProvider()).thenReturn("VIRTUAL_CHECKOUT");
        StreamAuthCookieService streamAuthCookieService = new StreamAuthCookieService();

        PublicConfigController controller = new PublicConfigController(checkoutSessionService, streamAuthCookieService);
        ReflectionTestUtils.setField(controller, "googleMapsApiKey", "  maps-key  ");
        ReflectionTestUtils.setField(controller, "googleOauthClientId", " , first-client-id , second-client-id ");
        ReflectionTestUtils.setField(controller, "paymentCurrency", " gbp ");
        ReflectionTestUtils.setField(controller, "privacyContactEmail", " privacy@club-portal.xyz ");
        ReflectionTestUtils.setField(controller, "dataControllerName", " Club Booking Portal Ltd ");
        ReflectionTestUtils.setField(controller, "retentionSummary", " Records are retained until manually deleted. ");
        ReflectionTestUtils.setField(controller, "processorsSummary", " Google Maps, Stripe, and AI chat providers. ");

        Map<String, Object> config = controller.getPublicConfig();

        assertEquals("maps-key", config.get("googleMapsApiKey"));
        assertEquals(Boolean.TRUE, config.get("googleMapsEnabled"));
        assertEquals("first-client-id", config.get("googleOauthClientId"));
        assertEquals(Boolean.TRUE, config.get("googleOauthEnabled"));
        assertEquals(Boolean.TRUE, config.get("paymentsEnabled"));
        assertEquals("VIRTUAL_CHECKOUT", config.get("paymentProvider"));
        assertEquals("GBP", config.get("paymentCurrency"));
        assertEquals("privacy@club-portal.xyz", config.get("privacyContactEmail"));
        assertEquals("Club Booking Portal Ltd", config.get("dataControllerName"));
        assertEquals("Records are retained until manually deleted.", config.get("retentionSummary"));
        assertEquals("Google Maps, Stripe, and AI chat providers.", config.get("processorsSummary"));
        assertEquals(604800L, config.get("authSessionTtlSeconds"));
        assertEquals(86400L, config.get("streamSessionTtlSeconds"));
        assertFalse(config.containsValue("sk_test_secret"));
    }
}
