package com.clubportal.controller;

import org.junit.jupiter.api.Test;
import com.clubportal.service.CheckoutSessionService;
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

        PublicConfigController controller = new PublicConfigController(checkoutSessionService);
        ReflectionTestUtils.setField(controller, "googleMapsApiKey", "  maps-key  ");
        ReflectionTestUtils.setField(controller, "googleOauthClientId", " , first-client-id , second-client-id ");
        ReflectionTestUtils.setField(controller, "paymentCurrency", " gbp ");

        Map<String, Object> config = controller.getPublicConfig();

        assertEquals("maps-key", config.get("googleMapsApiKey"));
        assertEquals(Boolean.TRUE, config.get("googleMapsEnabled"));
        assertEquals("first-client-id", config.get("googleOauthClientId"));
        assertEquals(Boolean.TRUE, config.get("googleOauthEnabled"));
        assertEquals(Boolean.TRUE, config.get("paymentsEnabled"));
        assertEquals("VIRTUAL_CHECKOUT", config.get("paymentProvider"));
        assertEquals("GBP", config.get("paymentCurrency"));
        assertFalse(config.containsValue("sk_test_secret"));
    }
}
