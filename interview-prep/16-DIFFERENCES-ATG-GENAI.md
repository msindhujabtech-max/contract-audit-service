# Differences (X vs Y) — ATG Commerce & Gen AI

Each has a quick table, followed by a plain-English explanation for when the table alone isn't enough.

# ATG / Oracle Commerce Differences

## Droplet vs Form Handler
| Droplet | Form Handler |
|---------|--------------|
| Displays dynamic content (view) | Processes form submissions (controller) |
| DynamoServlet | GenericFormHandler |
| oparam rendering | handleXxx() methods |
| Read/query data | Handle user actions |

**Explanation:** In ATG's page framework, these split view and controller duties. A **droplet** is a display component — it fetches or computes data and renders it into the JSP through output parameters (oparams); think "show me products in this category". A **form handler** processes user SUBMISSIONS — it takes form input, validates it, and performs actions like adding to cart or logging in, via `handleXxx()` methods. Droplet = display data; form handler = handle user actions.

## Nucleus vs Spring IoC
| Nucleus | Spring IoC |
|---------|------------|
| ATG's container | Spring's container |
| .properties config | Annotations/XML/Java |
| Predates Spring | Industry standard |
| Commerce-specific | General purpose |

**Explanation:** Both are dependency-injection containers that create and wire components. **Nucleus** is ATG's proprietary container — you define components and their dependencies in `.properties` files, and Nucleus instantiates and links them. **Spring IoC** is the general-purpose industry standard using annotations/XML/Java config. They share the same core idea (inversion of control); Nucleus is just ATG's commerce-specific, older take on it. Great analogy to give in an interview: "Nucleus is to ATG what the ApplicationContext is to Spring."

## Repository vs JPA/Hibernate
| ATG Repository (GSA) | JPA/Hibernate |
|----------------------|---------------|
| Item descriptors (XML) | Entities (annotations) |
| RQL queries | JPQL/Criteria |
| Built-in caching | Second-level cache |
| ATG-specific | Standard Java |

**Explanation:** Both are ways to map objects to database tables (ORM). ATG's **Repository** (via the Generic SQL Adapter) defines "item descriptors" in XML that map to tables, and you query with RQL (Repository Query Language); it has built-in caching. **JPA/Hibernate** is the standard Java ORM using annotated entity classes and JPQL/Criteria queries. Same concept (objects ↔ tables), but Repository is ATG-specific while JPA/Hibernate is the portable Java standard.

## ACC vs BCC
| ACC | BCC |
|-----|-----|
| ATG Control Center | Business Control Center |
| Desktop (Java Swing) | Web-based |
| Developer/admin, legacy | Business users |
| Older | Modern |

**Explanation:** Two admin tools in ATG. **ACC (ATG Control Center)** is the older desktop application (Java Swing) used mainly by developers/admins to manage components and data. **BCC (Business Control Center)** is the newer WEB-based tool designed for BUSINESS users to manage the catalog, promotions, pricing, and content, then publish changes. Trend is toward BCC for business self-service.

## DCS vs DPS
| DCS | DPS |
|-----|-----|
| Dynamo Commerce Suite | Dynamo Personalization Server |
| Catalog, cart, orders, pricing | Profiles, targeting, scenarios |
| Commerce | Personalization |

**Explanation:** Two core ATG modules. **DCS (Dynamo Commerce Suite)** handles the commerce essentials — product catalog, shopping cart, orders, pricing, and promotions. **DPS (Dynamo Personalization Server)** handles personalization — user profiles, targeting rules, and scenarios that tailor content to each user. DCS runs the store; DPS personalizes the experience.

## Endeca vs Elasticsearch
| Endeca | Elasticsearch |
|--------|---------------|
| Oracle guided navigation | Open-source search |
| Cartridges, MDEX | Inverted index, Lucene |
| Faceted commerce search | General full-text search |
| Licensed | Free/open |

**Explanation:** Both power search. **Endeca** is Oracle's commerce-focused search and guided-navigation engine (faceted filtering, the MDEX engine, reusable UI "cartridges") — licensed and tightly integrated with ATG. **Elasticsearch** is a general-purpose, open-source, distributed search engine built on Lucene — flexible full-text search at scale. At Nike you modernized search by moving toward Elasticsearch. Endeca = commerce-specialized/licensed; Elasticsearch = general/open/scalable.

## Scenario vs Slot vs Targeter (DPS)
| Scenario | Slot | Targeter |
|----------|------|----------|
| Event-driven rules | Content placeholder | Content selection rule |
| "if X then show Y" | Fills dynamically | Profile-based matching |

**Explanation:** ATG personalization pieces working together. A **scenario** is an event-driven rule ("if a user adds $100 to cart, then show free-shipping banner"). A **slot** is a placeholder on the page that gets filled with dynamic content at runtime. A **targeter** is a rule that selects WHICH content to show based on the user's profile. Flow: a scenario or targeter decides what content, and a slot displays it.

## Session vs Request vs Global scope (Nucleus)
| Global | Session | Request |
|--------|---------|---------|
| One instance (singleton) | Per user session | Per HTTP request |
| Shared config/services | Cart, profile | Transient data |

**Explanation:** Nucleus component scopes, mirroring web scopes. **Global** — one shared instance for the whole app (services, configuration) — like a singleton. **Session** — one instance per user session, lasting across their requests (shopping cart, logged-in profile). **Request** — a fresh instance for each HTTP request, holding transient per-request data. Choose based on how long the data should live and whether it's shared.

## ShippingGroup vs PaymentGroup
| ShippingGroup | PaymentGroup |
|---------------|--------------|
| How/where items ship | How order is paid |
| Address, method | Credit card, gift card |

**Explanation:** Two parts of an ATG order. A **ShippingGroup** captures HOW and WHERE items are delivered — shipping address and method; an order can have several (e.g., ship some items to home, some to office). A **PaymentGroup** captures HOW the order is paid — credit card, gift card, etc.; an order can split payment across several. Shipping = delivery details; payment = money details.

## commitOrder vs processOrder pipeline
| commitOrder | processOrder |
|-------------|--------------|
| Order submission (checkout) | Post-submission processing |
| Validate, pay, persist | Fulfillment steps |

**Explanation:** ATG uses pipelines (chains of processors) for order handling. The **commitOrder** pipeline runs when the customer SUBMITS the order at checkout — it validates the order, authorizes payment, and persists it. The **processOrder** pipeline handles what happens AFTER submission — fulfillment steps like inventory allocation and shipping. commitOrder = finalize the purchase; processOrder = fulfill it. Your resume's custom payment/pipeline work slots into commitOrder.

---

# Gen AI / RAG Differences

## RAG vs Fine-tuning
| RAG | Fine-tuning |
|-----|-------------|
| Retrieve context at query time | Retrain model weights |
| Cheap, fast to update | Expensive, static |
| Data stays external/private | Data baked into model |
| Add docs instantly | Retrain to update |
| Reduces hallucination | Changes behavior/style |

**Explanation:** Two ways to make an LLM use YOUR data. **RAG (Retrieval-Augmented Generation)** keeps your data in a vector database and, at query time, fetches the relevant pieces and injects them into the prompt — cheap, updates instantly (just add documents), keeps data private, and reduces hallucination. **Fine-tuning** actually RETRAINS the model's weights on your data — expensive, slow to update (retrain to change anything), and better for changing the model's style/behavior than for adding fresh knowledge. For document Q&A (your project), RAG is the right choice.

## RAG vs Prompt Engineering
| RAG | Prompt Engineering |
|-----|--------------------|
| Injects retrieved data | Crafts instructions |
| Solves knowledge gap | Guides behavior |
| Needs vector DB | Just the prompt |

**Explanation:** **Prompt engineering** is carefully wording the instructions you give the LLM to guide its behavior/format — no external data needed. **RAG** goes further by RETRIEVING relevant data from a knowledge base and inserting it into the prompt so the LLM can answer questions about information it wasn't trained on. Prompt engineering shapes HOW it answers; RAG gives it the FACTS to answer with. They're often used together (your system prompt + retrieved context).

## LangChain vs LangGraph
| LangChain | LangGraph |
|-----------|-----------|
| Linear chains | Stateful graphs |
| Sequential | Cyclic, branching |
| General orchestration | Complex agent workflows |
| Simpler | Multi-agent, loops |

**Explanation:** Both build LLM applications. **LangChain** links steps into mostly linear CHAINS (prompt → LLM → parse) — simple and great for straightforward pipelines. **LangGraph** models workflows as a GRAPH of nodes and edges, supporting cycles, branching, and shared state — needed for complex autonomous agents that must loop, retry, validate, or route dynamically, and for multi-agent collaboration. LangChain for linear flows; LangGraph for stateful, looping agent workflows.

## LangChain vs LlamaIndex
| LangChain | LlamaIndex |
|-----------|------------|
| General LLM framework | RAG/data-indexing focused |
| Agents, tools, chains | Ingestion, retrieval |
| Flexible orchestration | Best at search over data |

**Explanation:** Both help connect LLMs to data, with different strengths. **LangChain** is a broad framework for LLM apps — agents, tools, chains, memory — very flexible orchestration. **LlamaIndex** specializes in RAG — ingesting your documents, indexing them, and retrieving the right chunks efficiently. Use LlamaIndex when your focus is search/retrieval over data; LangChain when you need general orchestration, agents, and tools. Many projects combine them.

## Vector DB vs Traditional DB
| Vector DB | Traditional DB |
|-----------|----------------|
| Similarity search (embeddings) | Exact/range queries |
| Semantic meaning | Structured data |
| Cosine/Euclidean distance | WHERE, JOIN |
| pgvector, Pinecone | Oracle, MySQL |

**Explanation:** A **traditional database** answers exact/range queries on structured data ("find orders where price > 100") using WHERE and JOIN. A **vector database** stores embeddings (numeric meaning-vectors) and answers SIMILARITY queries ("find documents that MEAN something similar to this question") using distance metrics like cosine. Traditional DB = exact matching on structured fields; vector DB = semantic "find similar" search for AI. pgvector (which you used) adds vector search to PostgreSQL, so you get both in one.

## Embedding vs Token
| Embedding | Token |
|-----------|-------|
| Vector representing meaning | Text chunk (~4 chars) |
| Semantic similarity | Unit of processing |
| Fixed dimensions (768) | Variable count |

**Explanation:** A **token** is a small chunk of text (~4 characters or ~¾ of a word) — the unit an LLM reads and generates; a sentence is a sequence of tokens. An **embedding** is a fixed-length vector of numbers (e.g., 768 values) that captures the MEANING of a piece of text, so similar meanings sit close together in vector space. Tokens are how text is chopped up for processing; embeddings are how meaning is represented for similarity search.

## HNSW vs IVFFlat (vector index)
| HNSW | IVFFlat |
|------|---------|
| Graph-based | Cluster-based |
| Fast + accurate | Faster build, less accurate |
| More memory | Less memory |

**Explanation:** Two index types for speeding up vector similarity search. **HNSW** (Hierarchical Navigable Small World) builds a navigable graph — very fast and accurate queries, but uses more memory and takes longer to build (you chose this). **IVFFlat** groups vectors into clusters and only searches the nearest clusters — quicker to build and lighter on memory, but slightly less accurate. HNSW for best query speed/accuracy; IVFFlat when memory/build time matters more.

## Cosine vs Euclidean vs Dot Product
| Cosine | Euclidean (L2) | Dot Product |
|--------|----------------|-------------|
| Angle between vectors | Straight-line distance | Magnitude + direction |
| `<=>` | `<->` | `<#>` |
| Direction matters | Distance matters | Both |

**Explanation:** Three ways to measure how "similar" two embedding vectors are. **Cosine** measures the ANGLE between vectors — good when you care about direction/meaning regardless of magnitude (most common for text, what you used). **Euclidean (L2)** measures the straight-line DISTANCE between the points. **Dot product** combines both magnitude and direction. For semantic text search, cosine similarity is the usual default.

## LLM vs Traditional ML Model
| LLM | Traditional ML |
|-----|----------------|
| Pre-trained on huge text | Trained on specific dataset |
| General purpose | Task-specific |
| Prompt-driven | Feature engineering |
| Billions of params | Fewer params |

**Explanation:** A **traditional ML model** is trained on a specific dataset for a specific task (e.g., predict house prices from features you engineer) — smaller and narrow. An **LLM (Large Language Model)** is pre-trained on massive text and is general-purpose — you steer it with prompts (no retraining) and it has billions of parameters. Traditional ML = specialized tool built for one job; LLM = a versatile generalist you direct with instructions.

## Temperature 0 vs 1
| Temperature 0 | Temperature 1 |
|---------------|---------------|
| Deterministic | Creative/random |
| Same output | Varied output |
| Facts, code | Brainstorming |

**Explanation:** Temperature controls randomness in an LLM's output. **Temperature 0** — the model always picks the most likely next token, giving deterministic, consistent answers — best for facts, code, and your contract Q&A where accuracy matters (you set 0.2). **Temperature 1** (or higher) — the model takes more chances, producing varied, creative output — good for brainstorming or writing. Low temperature = precise and repeatable; high temperature = creative and varied.

## Zero-shot vs Few-shot Prompting
| Zero-shot | Few-shot |
|-----------|----------|
| No examples | Examples in prompt |
| "Translate this" | "cat→chat, dog→chien, bird→?" |

**Explanation:** Two prompting styles. **Zero-shot** — you just ask the task with NO examples ("Translate this to French") and rely on the model's general ability. **Few-shot** — you include a few EXAMPLES in the prompt to show the desired pattern ("cat→chat, dog→chien, bird→?"), which usually improves accuracy for tricky or specific formats. Zero-shot is simplest; few-shot guides the model when the task needs demonstration.

## Ollama (local) vs OpenAI API (cloud)
| Ollama (local) | OpenAI API (cloud) |
|----------------|--------------------|
| Runs on your hardware | Cloud-hosted |
| Data private | Data sent to provider |
| No API cost | Pay per token |
| Needs compute | No infra needed |
| Smaller models | State-of-the-art models |

**Explanation:** Two ways to run an LLM. **Ollama** runs models LOCALLY on your own hardware — your data never leaves your environment (privacy), there's no per-call cost, but you need enough compute and typically run smaller models (you used llama3.2:3b). **OpenAI API** calls powerful state-of-the-art models in the cloud — no infrastructure to manage, but you send data to a third party and pay per token. For private contracts (your project), local Ollama is the privacy-friendly choice.

## Agent vs Chain
| Agent | Chain |
|-------|-------|
| LLM decides steps/tools | Predefined sequence |
| Dynamic, autonomous | Fixed flow |
| Can loop, reason | Linear |

**Explanation:** In LLM apps, a **chain** is a FIXED, predefined sequence of steps (prompt → LLM → parse → return) — predictable. An **agent** lets the LLM DECIDE what to do next — which tools to call, whether to loop or retry — reasoning its way to a goal dynamically. Chains are like a fixed recipe; agents are like a chef who decides the next step based on how things are going. Agents are more powerful but less predictable.

## Semantic Search vs Keyword Search
| Semantic Search | Keyword Search |
|-----------------|----------------|
| Meaning-based (embeddings) | Exact word match |
| "car" finds "automobile" | "car" finds only "car" |
| Vector similarity | Inverted index |

**Explanation:** **Keyword search** matches the exact words you type (searching "car" won't find "automobile") using an inverted index — fast and precise for known terms. **Semantic search** matches MEANING using embeddings, so "car" can find "automobile" or "vehicle" because they're close in vector space — better when users phrase things differently. RAG relies on semantic search to find relevant chunks even when wording differs from the question.

## Distributed Tracing vs Logging vs Metrics (observability)
| Tracing | Logging | Metrics |
|---------|---------|---------|
| Request path across services | Event records | Numeric measurements |
| Zipkin/Jaeger | ELK/Loki | Prometheus |
| "where is the latency?" | "what happened?" | "how many/how much?" |

**Explanation:** The three pillars of observability, each answering a different question. **Logging** records discrete events ("what happened?") — good for debugging specific errors. **Metrics** are numeric measurements over time ("how many requests? how much memory?") — good for dashboards and alerts. **Distributed tracing** follows a single request as it hops across multiple services ("where did the time go?") — good for finding bottlenecks in microservices (you used Zipkin for this). Together they give a full picture of system health.
