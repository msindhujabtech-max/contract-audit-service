# Differences (X vs Y) — ATG Commerce & Gen AI

# ATG / Oracle Commerce Differences

## Droplet vs Form Handler
| Droplet | Form Handler |
|---------|--------------|
| Displays dynamic content (view) | Processes form submissions (controller) |
| DynamoServlet | GenericFormHandler |
| oparam rendering | handleXxx() methods |
| Read/query data | Handle user actions |

## Nucleus vs Spring IoC
| Nucleus | Spring IoC |
|---------|------------|
| ATG's container | Spring's container |
| .properties config | Annotations/XML/Java |
| Predates Spring | Industry standard |
| Commerce-specific | General purpose |

## Repository vs JPA/Hibernate
| ATG Repository (GSA) | JPA/Hibernate |
|----------------------|---------------|
| Item descriptors (XML) | Entities (annotations) |
| RQL queries | JPQL/Criteria |
| Built-in caching | Second-level cache |
| ATG-specific | Standard Java |

## ACC vs BCC
| ACC | BCC |
|-----|-----|
| ATG Control Center | Business Control Center |
| Desktop (Java Swing) | Web-based |
| Developer/admin, legacy | Business users |
| Older | Modern |

## DCS vs DPS
| DCS | DPS |
|-----|-----|
| Dynamo Commerce Suite | Dynamo Personalization Server |
| Catalog, cart, orders, pricing | Profiles, targeting, scenarios |
| Commerce | Personalization |

## Endeca vs Elasticsearch
| Endeca | Elasticsearch |
|--------|---------------|
| Oracle guided navigation | Open-source search |
| Cartridges, MDEX | Inverted index, Lucene |
| Faceted commerce search | General full-text search |
| Licensed | Free/open |

## Scenario vs Slot vs Targeter (DPS)
| Scenario | Slot | Targeter |
|----------|------|----------|
| Event-driven rules | Content placeholder | Content selection rule |
| "if X then show Y" | Fills dynamically | Profile-based matching |

## Session vs Request vs Global scope (Nucleus)
| Global | Session | Request |
|--------|---------|---------|
| One instance (singleton) | Per user session | Per HTTP request |
| Shared config/services | Cart, profile | Transient data |

## ShippingGroup vs PaymentGroup
| ShippingGroup | PaymentGroup |
|---------------|--------------|
| How/where items ship | How order is paid |
| Address, method | Credit card, gift card |

## commitOrder vs processOrder pipeline
| commitOrder | processOrder |
|-------------|--------------|
| Order submission (checkout) | Post-submission processing |
| Validate, pay, persist | Fulfillment steps |

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

## RAG vs Prompt Engineering
| RAG | Prompt Engineering |
|-----|--------------------|
| Injects retrieved data | Crafts instructions |
| Solves knowledge gap | Guides behavior |
| Needs vector DB | Just the prompt |

## LangChain vs LangGraph
| LangChain | LangGraph |
|-----------|-----------|
| Linear chains | Stateful graphs |
| Sequential | Cyclic, branching |
| General orchestration | Complex agent workflows |
| Simpler | Multi-agent, loops |

## LangChain vs LlamaIndex
| LangChain | LlamaIndex |
|-----------|------------|
| General LLM framework | RAG/data-indexing focused |
| Agents, tools, chains | Ingestion, retrieval |
| Flexible orchestration | Best at search over data |

## Vector DB vs Traditional DB
| Vector DB | Traditional DB |
|-----------|----------------|
| Similarity search (embeddings) | Exact/range queries |
| Semantic meaning | Structured data |
| Cosine/Euclidean distance | WHERE, JOIN |
| pgvector, Pinecone | Oracle, MySQL |

## Embedding vs Token
| Embedding | Token |
|-----------|-------|
| Vector representing meaning | Text chunk (~4 chars) |
| Semantic similarity | Unit of processing |
| Fixed dimensions (768) | Variable count |

## HNSW vs IVFFlat (vector index)
| HNSW | IVFFlat |
|------|---------|
| Graph-based | Cluster-based |
| Fast + accurate | Faster build, less accurate |
| More memory | Less memory |

## Cosine vs Euclidean vs Dot Product
| Cosine | Euclidean (L2) | Dot Product |
|--------|----------------|-------------|
| Angle between vectors | Straight-line distance | Magnitude + direction |
| `<=>` | `<->` | `<#>` |
| Direction matters | Distance matters | Both |

## LLM vs Traditional ML Model
| LLM | Traditional ML |
|-----|----------------|
| Pre-trained on huge text | Trained on specific dataset |
| General purpose | Task-specific |
| Prompt-driven | Feature engineering |
| Billions of params | Fewer params |

## Temperature 0 vs 1
| Temperature 0 | Temperature 1 |
|---------------|---------------|
| Deterministic | Creative/random |
| Same output | Varied output |
| Facts, code | Brainstorming |

## Zero-shot vs Few-shot Prompting
| Zero-shot | Few-shot |
|-----------|----------|
| No examples | Examples in prompt |
| "Translate this" | "cat→chat, dog→chien, bird→?" |

## Ollama (local) vs OpenAI API (cloud)
| Ollama (local) | OpenAI API (cloud) |
|----------------|--------------------|
| Runs on your hardware | Cloud-hosted |
| Data private | Data sent to provider |
| No API cost | Pay per token |
| Needs compute | No infra needed |
| Smaller models | State-of-the-art models |

## Agent vs Chain
| Agent | Chain |
|-------|-------|
| LLM decides steps/tools | Predefined sequence |
| Dynamic, autonomous | Fixed flow |
| Can loop, reason | Linear |

## Semantic Search vs Keyword Search
| Semantic Search | Keyword Search |
|-----------------|----------------|
| Meaning-based (embeddings) | Exact word match |
| "car" finds "automobile" | "car" finds only "car" |
| Vector similarity | Inverted index |

## Distributed Tracing vs Logging vs Metrics (observability)
| Tracing | Logging | Metrics |
|---------|---------|---------|
| Request path across services | Event records | Numeric measurements |
| Zipkin/Jaeger | ELK/Loki | Prometheus |
| "where is the latency?" | "what happened?" | "how many/how much?" |
