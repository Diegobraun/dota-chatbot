# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Dota 2 AI chatbot built with **Spring Boot 3.5.3** + **Spring AI 1.0.0**, backed by a local **Ollama** instance. It uses RAG (Retrieval-Augmented Generation) to answer questions about Dota 2 heroes and abilities in Portuguese.

> Note: The README.md is outdated — it still references the old Quarkus/LangChain4j stack. The project has been fully migrated to Spring Boot/Spring AI.

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
./mvnw test -Dtest=GreetingResourceTest

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
        └── DotaAssistant     Service wrapping ChatClient
            ├── System prompt: Dota 2-only scope enforcer (Portuguese)
            └── QuestionAnswerAdvisor  ← RAG retrieval from VectorStore
                └── RagConfig         Builds SimpleVectorStore on startup
                    ├── Cache: ./rag-cache.json (skip indexing on subsequent runs)
                    └── Source: classpath:rag/*.json (dota2_herois_habilidades.json)
```

### Key design decisions

- **RAG caching**: On first startup, `RagConfig` reads all files under `src/main/resources/rag/`, splits them with `TokenTextSplitter`, embeds them via Ollama (`nomic-embed-text`), and saves the `SimpleVectorStore` to `rag-cache.json` in the project root. Subsequent starts load from cache, skipping Ollama embedding calls. Delete `rag-cache.json` to force re-indexing.

- **`QuestionAnswerAdvisor`**: Spring AI advisor that intercepts user messages, retrieves relevant document chunks from the `VectorStore`, and appends them as context before calling the LLM.

- **Stateless chat**: Each `POST /dota` call is independent — there is no conversation memory between requests.

## Testing

Tests use `@WebMvcTest` + `@MockitoBean(DotaAssistant)` — they mock the AI layer entirely and only test the HTTP layer. No Ollama instance is needed to run tests.

## Configuration

All tuneable properties are in `src/main/resources/application.properties`:

| Property | Default | Purpose |
|---|---|---|
| `spring.ai.ollama.base-url` | `http://localhost:11434` | Ollama endpoint |
| `spring.ai.ollama.chat.options.model` | `llama3.2:latest` | Chat LLM |
| `spring.ai.ollama.embedding.options.model` | `nomic-embed-text` | Embedding model |
| `app.rag.cache-path` | `./rag-cache.json` | Vector store cache location |
