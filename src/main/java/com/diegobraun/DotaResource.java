package com.diegobraun;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dota")
@Tag(name = "Dota 2 Chatbot", description = "Assistente de IA especializado em Dota 2")
public class DotaResource {

    private final DotaAssistant dotaAssistant;

    public DotaResource(DotaAssistant dotaAssistant) {
        this.dotaAssistant = dotaAssistant;
    }

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(
            summary = "Enviar mensagem ao chatbot",
            description = "Envia uma pergunta sobre Dota 2 e recebe uma resposta gerada pelo modelo de linguagem com suporte a RAG.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Pergunta do usuário sobre Dota 2",
                    required = true,
                    content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(example = "Quais são as habilidades do Axe?"))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Resposta do assistente",
                            content = @Content(mediaType = MediaType.TEXT_PLAIN_VALUE, schema = @Schema(example = "Axe possui as habilidades: Berserker's Call, Battle Hunger, Counter Helix e Culling Blade.")))
            }
    )
    public String chat(@RequestBody String userMessage) {
        return dotaAssistant.chat(userMessage);
    }
}
