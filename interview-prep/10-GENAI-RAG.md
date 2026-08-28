# Gen AI, RAG & AI Engineering — Interview Preparation

This is your "Forward Deployed AI Engineer" upskilling story. Interviewers will probe how deep your understanding goes.

## Large Language Models (LLMs)

### What is an LLM?
A neural network trained on massive text data to predict the next token. Examples: GPT-4, Llama, Claude. They generate human-like text but have limitations: knowledge cutoff, hallucination, no access to private/current data.

### Key terms
| Term | Meaning |
|------|---------|
| **Token** | Chunk of text (~4 chars or ~0.75 words) |
| **Context window** | Max tokens the model can process at once |
| **Temperature** | Randomness (0 = deterministic, 1 = creative) |
| **Prompt** | Input instruction to the model |
| **Hallucination** | Model confidently generates false info |
| **Fine-tuning** | Further training on specific data |
| **Embedding** | Numeric vector representing meaning |

**In plain English:** These terms describe the knobs and units you work with when calling an LLM. A *token* is the billing and size unit — your contract PDF text gets split into tokens before the model sees it, and the *context window* caps how many tokens fit in one call. *Temperature* controls how "safe" vs "creative" the wording is, and an *embedding* is how text becomes searchable numbers (the foundation of RAG).

For example, in the contract-audit project a single question plus its retrieved context might look like this in token terms:

```
System prompt      ~120 tokens
Retrieved 3 chunks ~3000 tokens   (1000 tokens each)
User question      ~30 tokens
-------------------------------------------------
Total input        ~3150 tokens   (well under llama3.2's context window)
```

Because you want factual, grounded answers about contracts, you set `temperature = 0` (deterministic) rather than `1` (creative), so the same question yields the same answer.

---

## RAG (Retrieval-Augmented Generation) — your core project

### The problem RAG solves
LLMs don't know your private documents and have a knowledge cutoff. RAG lets the LLM answer using YOUR data without retraining.

### How RAG works
```
1. INGESTION (one-time):
   Documents → Split into chunks → Generate embeddings → Store in vector DB

2. RETRIEVAL + GENERATION (per query):
   User question → Embed question → Find similar chunks (vector search)
   → Inject chunks as context into the prompt → LLM generates answer
```

```
┌──────────────────────── RAG PIPELINE ────────────────────────┐
│                                                                │
│  PDF → chunks → [Embedding Model] → vectors → [Vector DB]     │
│                                                                │
│  Question → [Embedding Model] → vector → similarity search    │
│         → top-K chunks → prompt + context → [LLM] → Answer    │
└────────────────────────────────────────────────────────────────┘
```

> **Your project explanation:** "I built a RAG system where contract PDFs are chunked with TokenTextSplitter, embedded via Ollama's nomic-embed-text model, and stored in PostgreSQL with pgvector. On a query, I embed the question, do a cosine similarity search for the top 3 relevant chunks, inject them into the system prompt, and stream the LLM's answer."

### Why chunk documents?
- LLMs have limited context windows
- Smaller chunks = more precise retrieval
- Overlap between chunks preserves context at boundaries

### Chunking strategy (you used this)
```java
TokenTextSplitter splitter = new TokenTextSplitter(
    1000,  // chunk size (tokens)
    200,   // overlap
    5, 10000, true
);
```

---

## Embeddings & Vector Databases

### What is an embedding?
A vector (list of numbers) that captures the semantic meaning of text. Similar meanings → nearby vectors.
```
"dog"  → [0.2, 0.8, 0.1, ...]  (768 dimensions)
"puppy"→ [0.21, 0.79, 0.12, ...] (close to "dog")
"car"  → [0.9, 0.1, 0.5, ...]   (far from "dog")
```

### Vector similarity metrics
| Metric | Operator (pgvector) | Meaning |
|--------|---------------------|---------|
| Cosine distance | `<=>` | Angle between vectors (most common) |
| Euclidean (L2) | `<->` | Straight-line distance |
| Inner product | `<#>` | Dot product |

**In plain English:** All three answer the same question — "how close are two embeddings?" — but measure it differently. *Cosine distance* looks only at the angle (direction) between vectors and ignores their length, which is why it is the default for text search: two chunks about "payment terms" point the same way even if one is longer. *Euclidean* measures the actual gap between the vector tips, and *inner product* is a raw dot product often used when embeddings are already normalized.

In the contract-audit project you rank chunks with cosine distance using the `<=>` operator, and a smaller number means a closer match:

```sql
-- Lower distance = more relevant chunk to the question
SELECT content, embedding <=> '[0.21, 0.79, ...]' AS distance
FROM documents
ORDER BY distance     -- closest (smallest cosine distance) first
LIMIT 3;
```

### Vector database (pgvector — your choice)
```sql
CREATE EXTENSION vector;
CREATE TABLE documents (
    id SERIAL PRIMARY KEY,
    content TEXT,
    embedding vector(768)
);
-- Find similar
SELECT content FROM documents
ORDER BY embedding <=> '[...]' LIMIT 3;
```

### Indexes for vector search
- **HNSW** (Hierarchical Navigable Small World) — fast, accurate, more memory (you used this)
- **IVFFlat** — faster to build, less memory, slightly less accurate

**In plain English:** Without an index, pgvector compares your question against *every* stored chunk (a full scan) — fine for a few hundred rows, slow for millions. An index trades a little accuracy for a lot of speed. *HNSW* builds a layered "small world" graph of vectors so search hops quickly to near neighbors — it is the accurate-but-memory-hungry choice you used. *IVFFlat* groups vectors into buckets and only searches the closest buckets — cheaper to build but it can miss a match if the right bucket is skipped.

You create the HNSW index once after ingestion so contract queries stay fast as documents grow:

```sql
-- Build once; every similarity search then uses this graph index
CREATE INDEX ON documents
USING hnsw (embedding vector_cosine_ops);
```

---

## LangChain (your resume)

A framework for building LLM applications by "chaining" components.

### Core concepts
| Component | Purpose |
|-----------|---------|
| **Chains** | Sequence of calls (prompt → LLM → parse) |
| **Prompts** | Templated instructions |
| **Memory** | Conversation history |
| **Retrievers** | Fetch relevant documents |
| **Agents** | LLM decides which tools to use |
| **Tools** | Functions the agent can call |

```python
from langchain.chains import RetrievalQA
qa = RetrievalQA.from_chain_type(
    llm=llm,
    retriever=vectorstore.as_retriever()
)
answer = qa.run("What are the payment terms?")
```

---

## LangGraph (your resume) — advanced

A library for building **stateful, multi-step agent workflows** as graphs. Where LangChain is linear chains, LangGraph handles cycles, branching, and complex agent state.

### Why LangGraph?
- Agents that loop (retry, refine)
- Multiple agents collaborating
- Conditional routing between steps
- Human-in-the-loop checkpoints

```python
from langgraph.graph import StateGraph

graph = StateGraph(AgentState)
graph.add_node("retrieve", retrieve_docs)
graph.add_node("generate", generate_answer)
graph.add_node("validate", validate_answer)
graph.add_edge("retrieve", "generate")
graph.add_conditional_edges("validate",
    lambda s: "generate" if s.needs_retry else "end")
```

> **Talking point:** "LangGraph models agent workflows as a state graph with nodes and edges, enabling cyclic, conditional flows — ideal for autonomous agents that need to retry, validate, or route dynamically."

---

## LlamaIndex (your resume)

A data framework specialized for connecting LLMs to your data (RAG-focused). Strong at ingestion, indexing, and retrieval.

### LlamaIndex vs LangChain
| LlamaIndex | LangChain |
|------------|-----------|
| RAG/data-indexing focused | General LLM app framework |
| Best at retrieval | Best at orchestration/agents |
| Simpler for search | More flexible for workflows |

```python
from llama_index import VectorStoreIndex, SimpleDirectoryReader
docs = SimpleDirectoryReader("data").load_data()
index = VectorStoreIndex.from_documents(docs)
response = index.as_query_engine().query("What is the deadline?")
```

---

## Spring AI (your Java project)

Brings AI capabilities to the Spring ecosystem — LLM calls, embeddings, vector stores, RAG.
```java
// Chat
String answer = chatClient.prompt()
    .system("You are a contract analyst")
    .user(question)
    .call().content();

// Streaming (you used this)
Flux<String> stream = chatClient.prompt()
    .user(question).stream().content();

// Vector store
vectorStore.add(documents);
List<Document> similar = vectorStore.similaritySearch(query);
```

---

## Ollama (your project)

Runs LLMs locally (no cloud API needed). You used it for:
- Chat model: `llama3.2:3b`
- Embedding model: `nomic-embed-text`

> **Why local LLM?** Data privacy (contracts stay on-premise), no API costs, no rate limits. Trade-off: needs compute (your e2-standard-4 VM).

---

## Prompt Engineering

### Techniques
- **Zero-shot** — just ask, no examples
- **Few-shot** — provide examples in the prompt
- **Chain-of-Thought** — "think step by step"
- **System prompt** — set role/behavior (you used this to constrain answers to contract context)

**In plain English:** These are different ways of *asking* that change answer quality without retraining the model. *Zero-shot* just states the task; *few-shot* shows worked examples so the model copies the pattern; *chain-of-thought* asks it to reason step by step before answering (better for multi-step logic); and the *system prompt* sets the ground rules for the whole conversation. In RAG they stack together — the system prompt fences the model in, and few-shot examples shape the output format.

For example, a few-shot prompt for extracting a contract clause type shows the model the exact answer shape you want:

```
Classify the clause type. Examples:
Clause: "Either party may terminate with 30 days notice."  -> Termination
Clause: "Payment is due within 45 days of invoice."         -> Payment Terms

Clause: "The Provider shall indemnify the Client against..." ->
```

The model, having seen two labeled examples, returns `Indemnification` in the same one-word format instead of a rambling paragraph.

### Your system prompt strategy
```
"Only answer based on the provided context.
If context doesn't contain the answer, say
'I cannot find that information in the contract.'"
```
> This reduces hallucination by grounding the LLM strictly in retrieved data.

---

## AI Agents (Forward Deployed AI Engineer relevance)

An **agent** is an LLM that can reason, plan, and use tools to accomplish goals autonomously.

```
Agent loop:
1. Observe (input/state)
2. Think (LLM reasons about next step)
3. Act (call a tool — search, API, calculator)
4. Observe result → repeat until goal met
```

**Agent frameworks:** LangGraph, LangChain agents, CrewAI, AutoGPT.

---

## Common Gen AI Interview Questions

**Q: What is RAG and why use it over fine-tuning?**
> RAG retrieves relevant data at query time and injects it into the prompt. It's cheaper than fine-tuning, updates instantly (just add documents), reduces hallucination, and keeps data private. Fine-tuning changes model weights — expensive and static.

**Q: How do you reduce hallucination?**
> Ground the LLM with RAG (real context), use a strict system prompt, lower temperature, cite sources, and add a validation step. Instruct it to say "I don't know" when unsure.

**Q: What is an embedding and why 768 dimensions?**
> An embedding is a numeric vector capturing semantic meaning. 768 is the output dimension of the nomic-embed-text model — it's the model's fixed representation size.

**Q: How do you evaluate a RAG system?**
> Retrieval metrics (are the right chunks retrieved? precision/recall), generation metrics (faithfulness to context, answer relevance), and tools like RAGAS. Human evaluation for quality.

**Q: What is the difference between LangChain and LangGraph?**
> LangChain is for linear chains and general LLM orchestration. LangGraph handles stateful, cyclic, multi-agent workflows as graphs — better for complex autonomous agents.

**Q: What is a Forward Deployed Engineer?**
> An engineer who works directly in the client's environment, understanding their specific needs and deploying/customizing solutions on-site. For AI, it means embedding AI agents into complex real-world client systems — combining engineering, deployment, and client collaboration.

**Q: How would you deploy an AI agent into a client environment?**
> Containerize it (Docker), use local/private LLMs for data privacy (Ollama), integrate via APIs, add observability (tracing, logging), implement guardrails and fallbacks, and ensure it fits the client's security/compliance needs.
