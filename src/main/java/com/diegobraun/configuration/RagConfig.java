package com.diegobraun.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RagConfig {

    private static final Logger log = LoggerFactory.getLogger(RagConfig.class);

    @Value("${app.rag.cache-path:./rag-cache.json}")
    private String cachePath;

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) throws IOException {
        var vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        var cacheFile = new File(cachePath);

        if (cacheFile.exists()) {
            log.info("Carregando vector store do cache: {}", cacheFile.getAbsolutePath());
            vectorStore.load(cacheFile);
        } else {
            log.info("Cache nao encontrado, indexando documentos RAG...");
            indexDocuments(vectorStore);
            vectorStore.save(cacheFile);
            log.info("Vector store salvo em: {}", cacheFile.getAbsolutePath());
        }

        return vectorStore;
    }

    private void indexDocuments(SimpleVectorStore vectorStore) throws IOException {
        var resolver = new PathMatchingResourcePatternResolver();
        Resource[] resources = resolver.getResources("classpath:rag/*");

        var splitter = new TokenTextSplitter();
        List<Document> docs = new ArrayList<>();

        for (Resource resource : resources) {
            String content = resource.getContentAsString(StandardCharsets.UTF_8);
            docs.addAll(splitter.apply(List.of(new Document(content))));
        }

        if (!docs.isEmpty()) {
            vectorStore.add(docs);
        }
    }
}
