package com.clubportal.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.profiles.active=dev",
        "spring.datasource.url=jdbc:h2:mem:chatdebug;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "jwt.secret=debug-test-secret-12345678901234567890",
        "app.security.cors.allowed-origin-patterns=http://localhost:*",
        "app.llm.enabled=true",
        "app.llm.model=gpt-5-mini"
})
@AutoConfigureMockMvc
class ChatDebugControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingEndpointIsBlocked() throws Exception {
        mockMvc.perform(get("/api/debug/ping"))
                .andExpect(status().isForbidden());
    }

    @Test
    void chatVersionEndpointIsBlocked() throws Exception {
        mockMvc.perform(get("/api/debug/chat-version"))
                .andExpect(status().isForbidden());
    }

    @Test
    void otherDebugEndpointsRemainForbidden() throws Exception {
        mockMvc.perform(get("/api/debug/send-test-email"))
                .andExpect(status().isForbidden());
    }
}
