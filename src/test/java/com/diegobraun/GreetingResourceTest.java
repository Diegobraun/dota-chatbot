package com.diegobraun;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DotaResource.class)
class GreetingResourceTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DotaAssistant dotaAssistant;

    @Test
    void chatEndpointReturnsBotResponse() throws Exception {
        when(dotaAssistant.chat(anyString())).thenReturn("Axe é um herói de força.");

        mockMvc.perform(post("/dota")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Me fale sobre o Axe"))
                .andExpect(status().isOk())
                .andExpect(content().string("Axe é um herói de força."));
    }
}
