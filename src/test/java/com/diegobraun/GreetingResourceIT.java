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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DotaResource.class)
class GreetingResourceIT {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    DotaAssistant dotaAssistant;

    @Test
    void chatEndpointRespondsSuccessfully() throws Exception {
        when(dotaAssistant.chat(anyString())).thenReturn("Resposta sobre Dota 2.");

        mockMvc.perform(post("/dota")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Qual herói tem mais vida?"))
                .andExpect(status().isOk());
    }
}
