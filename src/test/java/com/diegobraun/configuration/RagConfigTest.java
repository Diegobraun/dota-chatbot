package com.diegobraun.configuration;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RagConfigTest {

    @TempDir
    Path tempDir;

    private RagConfig ragConfig;
    private EmbeddingModel embeddingModel;
    private Path cachePath;

    @BeforeEach
    void setup() {
        ragConfig = new RagConfig();
        embeddingModel = mock(EmbeddingModel.class);
        cachePath = tempDir.resolve("test-rag-cache.json");
        ReflectionTestUtils.setField(ragConfig, "cachePath", cachePath.toString());
    }

    @Test
    void contentRetriever_loadsFromCacheWhenFileExists() throws IOException {
        InMemoryEmbeddingStore<TextSegment> store = new InMemoryEmbeddingStore<>();
        Files.writeString(cachePath, store.serializeToJson());

        ContentRetriever retriever = ragConfig.contentRetriever(embeddingModel);

        assertThat(retriever).isNotNull();
        verifyNoInteractions(embeddingModel);
    }

    @Test
    void contentRetriever_indexesAndSavesCacheWhenFileAbsent() throws IOException {
        Embedding fakeEmbedding = Embedding.from(new float[]{0.1f, 0.2f, 0.3f});
        when(embeddingModel.embed(any(TextSegment.class)))
                .thenReturn(Response.from(fakeEmbedding));

        ContentRetriever retriever = ragConfig.contentRetriever(embeddingModel);

        assertThat(retriever).isNotNull();
        assertThat(cachePath).exists();
        verify(embeddingModel, atLeastOnce()).embed(any(TextSegment.class));
    }
}
