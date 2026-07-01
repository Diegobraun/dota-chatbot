# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Dota 2 AI chatbot built with **Spring Boot 3.5.3** + **LangChain4j 1.0.0-beta3**, backed by a local **Ollama** instance. It uses RAG (Retrieval-Augmented Generation) to answer questions about Dota 2 heroes and abilities in Portuguese.

## Prerequisites

Ollama must be running locally before starting the app:
- Chat model: `llama3.2:latest`
- Embedding model: `nomic-embed-text`

```bash
ollama pull llama3.2:latest
ollama pull nomic-embed-text
ollama serve
```

## Common Commands

```bash
# Build
./mvnw package

# Run
./mvnw spring-boot:run

# Run tests only
./mvnw test

# Run a single test class
./mvnw test -Dtest=DotaResourceTest

# Clean build
./mvnw clean package
```

The app starts on `http://localhost:8080`.  
Swagger UI: `http://localhost:8080/swagger-ui.html`  
OpenAPI spec: `http://localhost:8080/api-docs`

## Architecture

```
POST /dota  (text/plain)
    └── DotaResource          REST controller
        └── DotaAssistant     @AiService interface (LangChain4j)
            ├── @SystemMessage: Dota 2-only scope enforcer (Portuguese)
            └── ContentRetriever  ← RAG retrieval from InMemoryEmbeddingStore
                └── RagConfig     Builds EmbeddingStore on startup
                    ├── Cache: ./rag-cache.json (skip indexing on subsequent runs)
                    └── Source: classpath:rag/*.json (dota2_herois_habilidades.json)
```

### Key design decisions

- **RAG caching**: On first startup, `RagConfig` reads all files under `src/main/resources/rag/`, splits them with `DocumentSplitters.recursive(512, 0)`, embeds them one-by-one via Ollama (`nomic-embed-text`), and serializes the `InMemoryEmbeddingStore` to `rag-cache.json`. Subsequent starts load from cache. Delete `rag-cache.json` to force re-indexing.

- **Embed one-by-one**: `embedAll` with ~2700 segments causes an EOF error in Ollama's internal tokenizer. Segments are embedded individually via `embeddingModel.embed(segment)` to avoid this.

- **`@AiService`**: LangChain4j Spring Boot starter scans for interfaces annotated with `@AiService` and creates the implementation automatically, wiring `ChatLanguageModel` and `ContentRetriever` beans.

- **Stateless chat**: Each `POST /dota` call is independent — there is no conversation memory between requests.

## Testing

Tests use `@WebMvcTest` + `@MockitoBean(DotaAssistant)` — they mock the AI layer entirely and only test the HTTP layer. No Ollama instance is needed to run tests.

## Configuration

All tuneable properties are in `src/main/resources/application.yml`:

| Property | Default | Purpose |
|---|---|---|
| `langchain4j.ollama.chat-model.base-url` | `http://localhost:11434` | Ollama endpoint |
| `langchain4j.ollama.chat-model.model-name` | `llama3.2:latest` | Chat LLM |
| `langchain4j.ollama.embedding-model.base-url` | `http://localhost:11434` | Ollama endpoint |
| `langchain4j.ollama.embedding-model.model-name` | `nomic-embed-text` | Embedding model |
| `app.rag.cache-path` | `./rag-cache.json` | Vector store cache location |
| `app.rag.max-results` | `5` | Segments retrieved per query |
| `app.rag.chunk-size` | `512` | Document splitter chunk size |
| `app.rag.chunk-overlap` | `0` | Document splitter chunk overlap |
