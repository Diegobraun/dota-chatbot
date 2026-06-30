# dota-chatbot

Chatbot de IA especializado em Dota 2, construído com **Spring Boot 3.5.3** e **LangChain4j 1.0.0-beta3**, utilizando um modelo local via **Ollama** e RAG (Retrieval-Augmented Generation) para responder perguntas sobre heróis, habilidades e o universo do jogo.

## Pré-requisitos

- Java 21+
- [Ollama](https://ollama.com/) instalado e rodando localmente

### Modelos necessários

```bash
ollama pull llama3.2:latest
ollama pull nomic-embed-text
ollama serve
```

## Executando a aplicação

```bash
./mvnw spring-boot:run
```

A aplicação sobe em `http://localhost:8080`.

## Endpoints

| Método | Path | Content-Type | Descrição |
|--------|------|-------------|-----------|
| `POST` | `/dota` | `text/plain` | Envia uma pergunta e recebe a resposta do chatbot |

**Exemplo:**
```bash
curl -X POST http://localhost:8080/dota \
  -H "Content-Type: text/plain" \
  -d "Quais são as habilidades do Axe?"
```

### Documentação da API

- [Swagger UI](http://localhost:8080/swagger-ui.html) — `http://localhost:8080/swagger-ui.html`
- [OpenAPI spec](http://localhost:8080/api-docs) — `http://localhost:8080/api-docs`

## Arquitetura

```mermaid
flowchart TD
    User(["👤 Cliente\nPOST /dota\ntext/plain"])

    subgraph App["🌱 Spring Boot App"]
        Controller["DotaResource\n@RestController"]
        Assistant["DotaAssistant\n@AiService interface"]

        subgraph ChatPipeline["LangChain4j Pipeline"]
            SystemPrompt["@SystemMessage\nEscopo: somente Dota 2\nIdioma: Português"]
            Retriever["ContentRetriever\nRAG · injeta contexto\nantes do LLM"]
        end

        subgraph RAGSetup["RagConfig  (inicialização)"]
            CacheCheck{"rag-cache.json\nexiste?"}
            LoadCache["Carrega EmbeddingStore\ndo cache JSON"]
            ReadDocs["Lê documentos\nclasspath:rag/*.json"]
            Split["DocumentSplitters.recursive\nDivide em chunks de 512 chars"]
            SaveCache["Serializa EmbeddingStore\nem rag-cache.json"]
        end

        VectorStore[("InMemoryEmbeddingStore\n(in-memory)")]
    end

    subgraph Ollama["🦙 Ollama  :11434"]
        EmbedModel["nomic-embed-text\nEmbedding"]
        ChatModel["llama3.2:latest\nChat LLM"]
    end

    %% Fluxo de requisição
    User -->|pergunta| Controller
    Controller --> Assistant
    SystemPrompt --> Retriever
    Assistant --> Retriever
    Retriever -->|busca vetorial| VectorStore
    VectorStore -->|chunks relevantes| Retriever
    Retriever -->|pergunta + contexto| ChatModel
    ChatModel -->|resposta| Assistant
    Assistant -->|resposta| Controller
    Controller -->|text/plain| User

    %% Fluxo de inicialização RAG
    CacheCheck -->|"sim (2ª execução+)"| LoadCache
    CacheCheck -->|"não (1ª execução)"| ReadDocs
    ReadDocs --> Split
    Split -->|chunks individuais| EmbedModel
    EmbedModel -->|vetores float| VectorStore
    VectorStore --> SaveCache
    LoadCache --> VectorStore
```

## Como funciona o RAG

Na primeira inicialização, a aplicação lê os documentos em `src/main/resources/rag/`, gera embeddings via Ollama (`nomic-embed-text`) — um segmento por vez — e salva o índice vetorial em `rag-cache.json` na raiz do projeto. Nas execuções seguintes, o cache é reutilizado, evitando o custo de reindexação.

Para forçar a reindexação, basta deletar o arquivo `rag-cache.json`.

## Build e testes

```bash
# Compilar e empacotar
./mvnw package

# Rodar testes
./mvnw test

# Build + testes
./mvnw verify
```

Os testes usam `@WebMvcTest` com mock do `DotaAssistant`, portanto **não exigem** Ollama rodando.

## Configuração

As propriedades estão em `src/main/resources/application.properties`:

| Propriedade | Padrão | Descrição |
|-------------|--------|-----------|
| `langchain4j.ollama.chat-model.base-url` | `http://localhost:11434` | URL do Ollama (chat) |
| `langchain4j.ollama.chat-model.model-name` | `llama3.2:latest` | Modelo de chat |
| `langchain4j.ollama.embedding-model.base-url` | `http://localhost:11434` | URL do Ollama (embedding) |
| `langchain4j.ollama.embedding-model.model-name` | `nomic-embed-text` | Modelo de embedding |
| `app.rag.cache-path` | `./rag-cache.json` | Caminho do cache vetorial |
