# Wippy Platform — LLM Agent Context & Rules

> Source: https://home.wj.wippy.ai/llms.txt, official blog, GitHub repos
> Purpose: Reference for building IntelliJ IDEA plugin for Wippy integration
> Updated: 2026-02-08

---

## 1. What is Wippy

Wippy is a **self-modifying agentic platform** — an intelligent application runtime where code rewrites itself.
Built by **Spiral Scout** (San Francisco). The runtime is written in **Go**, user-facing scripting is **Lua**.
Everything deploys as a **single binary**. Apache 2.0 License.

**Tagline**: "Run code that writes itself."

Closest analogy: **BabyAGI + Erlang OTP**, designed for AI agents with a governance pipeline on top.

### Core Properties
- Self-modifying: agents read, modify, and create code + config at runtime
- Single binary deployment (Go runtime + Lua engine + core services + user logic)
- Actor-based concurrency via lightweight Lua processes
- Distributed, versioned registry as the central nervous system
- Native LLM integration (OpenAI, Anthropic Claude, Google Vertex AI, OpenRouter, local models)
- ~99% of the application codebase is changeable from inside Wippy itself at runtime

---

## 2. Registry — Central Nervous System

The registry is a **distributed, versioned key-value store** organized by namespaces.
ALL system configuration and application logic lives here as entries (YAML + Lua).

- **Unified config**: HTTP services, DB connections, Lua functions, AI agents — all are entries
- **Atomic updates & rollbacks** via changesets → immutable versions
- **Audit trail**: `registry.versions`, instant rollback: `registry.apply_version`
- **Dynamic loading**: runtime loads components from registry state, live updates without restart
- **Lua API**: `registry.get`, `registry.find`, `snapshot:changes`, `changes:apply`
- **Self-modification**: Lua code can modify system behavior including its own code
- **RSI safety**: security checks for Recursive Self-Improvement loops

---

## 3. Entry Kinds (Registry Entry Types)

All components are **registry entries** in YAML. Key kinds:

| Kind | File ext | Description |
|------|----------|-------------|
| `function.lua` | `.lua` | Primary unit of logic. Can be API endpoint or AI tool |
| `library.lua` | `.lua` | Reusable Lua library |
| `process.lua` | `.lua` | Long-running Lua process (actor) |
| `agent.gen1` | `.yaml` | Declarative AI agent definition |
| `http.endpoint` | `.yaml` | URL path + HTTP method → `function.lua` handler |
| `http.router` | `.yaml` | Route groups with middleware (CORS, auth, logging) |
| `http.server` | `.yaml` | HTTP server configuration |
| `http.static` | `.yaml` | Static file serving |
| `http.middleware` | `.yaml` | Middleware definition |
| `websocket.relay` | `.yaml` | WebSocket relay configuration |
| `security.policy` | `.yaml` | Access control policy |
| `security.token_store` | `.yaml` | Authentication token management |
| `embedding_repo` | `.yaml` | Vector embedding repository config |

### Entry Metadata Fields
- `meta.type=tool` — exposes `function.lua` as an AI agent tool
- `meta.input_schema` — JSON schema for AI tool inputs

### Entry YAML Structure (typical)
```yaml
kind: function.lua
namespace: myapp.api
name: get_users
meta:
  type: tool
  input_schema:
    type: object
    properties:
      query:
        type: string
body: |
  local sql = require("sql")
  -- function body here
```

### Namespace Convention
Entries organized by **dot-separated namespaces**: `myapp.api`, `myapp.agents`, `wippy.llm`.
Each entry has a unique path within its namespace.

---

## 4. Actor-Based Concurrency

Wippy uses the **actor model** via lightweight Lua processes:

- Each process: **isolated state**, communicates via **async message passing**
- `process.send`, `process.listen`, `process.inbox` — message API
- Runtime manages **thousands** of concurrent Lua processes
- Supervision trees: `process.spawn_monitored`, `process.spawn_linked`
- Go-like channels: `channel.new`, `channel.select`
- **Event bus**: async pub/sub, reactive workflows

---

## 5. AI / LLM Integration (First-Class)

### 5.1 Unified LLM Interface (`wippy.llm.spec`)
```lua
llm.generate(...)           -- text generation
llm.structured_output(...)  -- force JSON schema output
llm.embed(...)              -- vector embeddings
```

Providers: `wippy.llm.openai:*`, `wippy.llm.claude:*`, `wippy.llm.google.vertex:*`

### 5.2 Declarative Agents (`agent.gen1`)
```yaml
kind: agent.gen1
name: my_agent
prompt: "You are a helpful assistant..."
model: claude-3-sonnet
temperature: 0.7
max_tokens: 4096
thinking_effort: high        # enhanced reasoning
traits:
  - conversational
  - thinking
  - technical_expertise
memory: [...]                 # persistent context
tools:
  - myapp.tools.search
  - myapp.tools.query_db
delegates_to:
  - myapp.agents.specialist
inherits_from: myapp.agents.base
```

### 5.3 System Agents
- `wippy.registry` — interactive registry management
- `wippy.coder` — code generation/modification
- `wippy.git` — Git synchronization (GitOps)

### 5.4 Embeddings & Semantic Search
- `llm.embed` → store in SQL (PostgreSQL/SQLite + vector extensions)
- `embedding_repo.search_by_embedding` → similarity search
- Enables RAG and semantic search

### 5.5 Prompt Engineering (`wippy.llm:prompt`)
- Multi-turn, multi-modal (text + images)
- Roles: system, user, assistant, function calls/results

### 5.6 Self-Modification via AI
- System reflection: agents read registry to understand own config + code
- Controlled modification: agents alter entries (config, code, agents)
- Conversational development: natural language → system changes

---

## 6. Lua Runtime

### Why Lua
- Simple, lightweight, embeddable, fast
- Specifically adapted for LLM interaction
- Easy for AI to read, write, analyze, and modify

### Available Modules

| Category | Modules |
|----------|---------|
| **Registry** | `registry` |
| **AI/LLM** | `llm`, `prompt`, `agent_runner`, `embeddings` |
| **Concurrency** | `process`, `channel`, `actor` |
| **Networking** | `http`, `http_client`, `websocket` |
| **Data** | `json`, `yaml`, `base64`, `compress`, `payload`, `excel` |
| **Storage** | `sql`, `store`, `fs`, `cloudstorage`, `queue` |
| **System** | `exec`, `time`, `logger`, `env`, `crypto`, `uuid`, `io`, `metrics`, `ostime` |
| **Text** | `text`, `template` (Jet engine), `treesitter` |
| **Security** | `security`, `crypto`, `hash`, `uuid` |
| **Dynamic** | `eval`, `exec`, `expression` |

### Tree-sitter Integration
Built-in code parsing and analysis directly in Lua for AI-driven code understanding.

---

## 7. HTTP System

- **Server**: `http.server` entries, TLS, custom ports
- **Router**: `http.router` with middleware chains (CORS, auth, logging, rate limit)
- **Endpoints**: `http.endpoint` maps URL+method → `function.lua`
- **WebSocket**: full support, `websocket.relay` for proxy patterns
- **Static**: `http.static` serves from filesystem or cloud

---

## 8. Storage

- **SQL** (`lua/storage/sql`) — PostgreSQL, SQLite, vector extensions. Largest module (~29K docs)
- **Store** — key-value store
- **Filesystem** — local FS operations
- **Cloud Storage** — S3-compatible
- **Queue** — async task processing

---

## 9. Temporal Integration

Wippy integrates with **Temporal.io** for durable workflow orchestration:
- Long-running fault-tolerant **workflows** (23.1K docs)
- **Activities** with retry policies, timeouts, heartbeats
- State management, versioning, signals, queries

---

## 10. Security

- Granular policies: `security.policy` entries grouped into scopes
- `security.can()` for permission checks in Lua
- Token stores for authentication
- Runtime-level security checks (Go layer, outside LLM space)
- RSI loop protection, process isolation

---

## 11. Development Tools

- **CLI**: project management, registry operations, dev server
- **Linter**: code quality for Lua and YAML
- **Language Server (LSP)**: autocompletion, diagnostics, go-to-definition
- **Git sync**: `wippy.git` agent, registry ↔ Git, GitOps workflows
- **Dependency management**: module dependencies, version resolution, publishing

---

## 12. Documentation API

Base URL: `https://home.wj.wippy.ai`

| Endpoint | Use |
|----------|-----|
| `GET /llm/toc` | JSON table of contents |
| `GET /llm/search?q=<query>` | Search → chunk IDs |
| `GET /llm/path/en/<path>` | Full page content |
| `GET /llm/summary/en/<path>` | Quick overview |
| `GET /llm/context?paths=a,b` | Batch fetch pages |
| `GET /llm/chunk/<id>` | Get chunk by ID |
| `GET /llm/related/<id>` | Related content |
| `GET /llms.txt` | Human/LLM index |

Graph navigation: each section has ID `path#anchor` (e.g. `http/routing#middleware`).

### Full Documentation Map (100 pages, ~137K tokens)

**Getting Started**: `start/about` (2.3K), `start/installation` (1.0K), `start/structure` (4.7K)

**Guides**: `guides/cli` (6.6K), `guides/configuration` (7.2K), `guides/linter` (5.1K), `guides/lsp` (4.1K), `guides/dependency-management` (4.8K), `guides/entry-kinds` (12.3K), `guides/observability` (4.3K), `guides/queue-consumers` (3.0K), `guides/supervision` (4.9K), `guides/publishing` (5.2K)

**Core Concepts**: `concepts/compute-units` (2.8K), `concepts/registry` (1.6K), `concepts/process-model` (5.4K), `concepts/functions` (4.0K), `concepts/workflows` (4.4K)

**Lua Runtime**: `lua/overview` (3.1K), `lua/types` (9.1K), `lua/entries` (3.7K)

**Lua Core**: `lua/core/base` (9.6K), `lua/core/errors` (2.7K), `lua/core/time` (13.3K), `lua/core/channel` (4.5K), `lua/core/process` (8.2K), `lua/core/funcs` (7.3K), `lua/core/future` (2.0K), `lua/core/stream` (2.4K), `lua/core/context` (0.8K), `lua/core/events` (2.9K), `lua/core/registry` (3.8K), `lua/core/contract` (4.3K)

**Lua Data**: `lua/data/json` (4.9K), `lua/data/yaml` (3.0K), `lua/data/base64` (2.2K), `lua/data/compress` (7.0K), `lua/data/payload` (3.3K), `lua/data/excel` (5.5K)

**Lua HTTP**: `lua/http/http` (10.7K), `lua/http/client` (8.2K), `lua/http/websocket` (6.8K), `lua/http/html` (7.2K)

**Lua Storage**: `lua/storage/sql` (29.0K), `lua/storage/store` (3.1K), `lua/storage/filesystem` (4.7K), `lua/storage/cloud` (6.5K), `lua/storage/queue` (3.2K)

**Lua System**: `lua/system/system` (6.1K), `lua/system/env` (3.0K), `lua/system/logger` (1.7K), `lua/system/io` (2.0K), `lua/system/metrics` (2.0K), `lua/system/ostime` (4.3K)

**Lua Text**: `lua/text/text` (8.9K), `lua/text/template` (2.7K), `lua/text/treesitter` (10.0K)

**Lua Security**: `lua/security/security` (7.4K), `lua/security/crypto` (5.0K), `lua/security/hash` (3.4K), `lua/security/uuid` (2.9K)

**Lua Dynamic**: `lua/dynamic/eval` (5.3K), `lua/dynamic/exec` (3.8K), `lua/dynamic/expression` (3.8K)

**Temporal**: `temporal/overview` (5.5K), `temporal/activities` (8.5K), `temporal/workflows` (23.1K)

**System Components**: `system/process-host` (1.4K), `system/terminal` (1.0K), `system/database` (6.1K), `system/store` (1.8K), `system/queue` (3.8K), `system/filesystem` (1.4K), `system/cloudstorage` (1.7K), `system/env` (3.3K), `system/template` (2.5K), `system/security` (12.4K), `system/exec` (3.2K)

**HTTP**: `http/server` (3.8K), `http/router` (5.7K), `http/endpoint` (5.0K), `http/middleware` (6.6K), `http/static` (3.8K), `http/websocket-relay` (5.0K)

**Tutorials**: `tutorials/hello-world` (2.7K), `tutorials/cli` (3.6K), `tutorials/echo-service` (7.7K), `tutorials/channels` (4.4K), `tutorials/processes` (10.2K), `tutorials/supervision` (15.6K), `tutorials/task-queue` (9.4K), `tutorials/auth` (16.8K)

**Internals**: `internals/registry` (3.6K), `internals/scheduler` (4.4K), `internals/dispatch` (4.2K), `internals/events` (6.0K), `internals/modules` (10.0K), `internals/kinds` (3.0K)

---

## 13. Plugin Development Notes

### Wippy-specific features to implement:
1. **Registry entry recognition** — understand YAML entry kinds and schemas
2. **Lua module awareness** — autocompletion for Wippy modules (`registry`, `llm`, `process`, etc.)
3. **Entry kind validation** — validate YAML structure per `kind` field
4. **Navigation** — jump between YAML config ↔ linked Lua files
5. **Agent definition support** — traits, tools, memory, delegation in `agent.gen1`
6. **Namespace awareness** — dot-separated namespace conventions
7. **Tool schema validation** — validate `meta.input_schema` JSON schemas
8. **Doc integration** — fetch docs from `/llm/*` endpoints
9. **Registry references** — resolve cross-entry references (tool refs in agents, endpoint → function)
10. **Linter integration** — connect to Wippy's built-in linter

### Lua support
Taken from existing plugin initially. Focus on **Wippy-specific features first**.

---

## 14. Production Reference
- Fortress case study: 50+ agents, 800+ tools, multi-model (Claude/GPT/Gemini), Salesforce two-way sync
- Wippy Local: desktop app, runs fully locally, provider-agnostic
- GitHub: https://github.com/wippyai

---

## 15. LSP Integration (LSP4IJ)

Wippy uses **LSP4IJ** for providing intelligent language features (completions, diagnostics, etc.) for both Lua and YAML files.

### Server Configuration
- **Server Type**: LSP4IJ (Red Hat)
- **ID**: `wippyLanguageServer`
- **Connection**: Socket (TCP) on `localhost:7777`
- **Manual Start**: The LSP server is started manually by the user or external process.

### Features Provided via LSP
- **Completions**: Registry awareness, Lua module completions (`llm.*`, `process.*`, `registry.*`).
- **Diagnostics**: Built-in Wippy linter results.
- **Navigation**: Definition/Declaration support for cross-entry references.
- **Signature Help**: Parameter info for Lua functions and Wippy APIs.
- **Structure View**: Document symbols for YAML registry entries and Lua files.
- **Folding**: Standard code folding.

### Document Matching
- Only active for `.lua`, `.yaml`, and `.yml` files.
- Uses `WippyDocumentMatcher` to limit activation to files belonging to a Wippy project (presence of `wippy.lock`, `.wippy.yaml`, or `.wippy` directory).