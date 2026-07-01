package com.diegobraun.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.rag")
public record RagProperties(
        @DefaultValue("./rag-cache.json") String cachePath,
        @DefaultValue("5") int maxResults,
        @DefaultValue("512") int chunkSize,
        @DefaultValue("0") int chunkOverlap
) {
}
