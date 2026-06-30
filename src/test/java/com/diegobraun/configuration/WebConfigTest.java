package com.diegobraun.configuration;

import com.diegobraun.DotaAssistant;
import com.diegobraun.DotaResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DotaResource.class)
class WebConfigTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DotaAssistant dotaAssistant;

    @Test
    void corsAllowsLocalhost5173ForPost() throws Exception {
        mockMvc.perform(options("/dota")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsAllowsLocalhost5173ForGet() throws Exception {
        mockMvc.perform(options("/dota")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void corsBlocksDisallowedOrigin() throws Exception {
        mockMvc.perform(options("/dota")
                        .header("Origin", "http://malicious.com")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
