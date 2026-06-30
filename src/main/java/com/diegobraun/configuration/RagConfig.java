package com.diegobraun.configuration;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${app.rag.cache-path:./rag-cache.json}")
    private String cachePath;

    @Bean
    public ContentRetriever contentRetriever(EmbeddingModel embeddingModel) throws IOException {
        InMemoryEmbeddingStore<TextSegment> embeddingStore;
        File cacheFile = new File(cachePath);

        if (cacheFile.exists()) {
            log.info("Carregando vector store do cache: {}", cacheFile.getAbsolutePath());
            String json = Files.readString(cacheFile.toPath());
            embeddingStore = InMemoryEmbeddingStore.fromJson(json);
        } else {
            log.info("Cache nao encontrado, indexando documentos RAG...");
            embeddingStore = new InMemoryEmbeddingStore<>();
            indexDocuments(embeddingStore, embeddingModel);
            Files.writeString(cacheFile.toPath(), embeddingStore.serializeToJson());
            log.info("Vector store salvo em: {}", cacheFile.getAbsolutePath());
        }

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)
                .build();
    }

    private void indexDocuments(InMemoryEmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:rag/*");

        DocumentSplitter splitter = DocumentSplitters.recursive(512, 0);
        List<TextSegment> segments = new ArrayList<>();

        for (Resource resource : resources) {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            segments.addAll(splitter.split(Document.from(content)));
        }

        if (!segments.isEmpty()) {
            List<Embedding> embeddings = embeddingModel.embedAll(segments).content();
            embeddingStore.addAll(embeddings, segments);
        }
    }
}
