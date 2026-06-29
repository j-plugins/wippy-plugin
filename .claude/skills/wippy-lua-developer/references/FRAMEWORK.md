# Wippy Framework Modules — Reference

The AI/agent framework ships as installable modules declared via `ns.dependency` (see
ENTRIES-AND-CONFIG → Dependencies). All require an `env.storage.os` (for API keys) and a
`process.host`. Source: `framework/llm`, `framework/agents`, `framework/testing`.

Framework modules: **`wippy/llm`** (LLM), **`wippy/agent`** (agents), **`wippy/test`** (testing),
**`wippy/dataflow`** (DAG orchestration), **`wippy/relay`** (WebSocket relay), **`wippy/views`**
(pages/components), **`wippy/facade`** (frontend iframe facade).

---

## wippy/llm — unified LLM interface

Providers: OpenAI, Anthropic, Google, local models. Supports text generation, tool calling,
structured output, embeddings, streaming.

### Setup
```bash
wippy add wippy/llm
wippy install
```
```yaml
entries:
  - { name: os_env, kind: env.storage.os }
  - name: processes
    kind: process.host
    lifecycle: { auto_start: true }
  - name: dep.llm
    kind: ns.dependency
    component: wippy/llm
    version: "*"
    parameters:
      - { name: env_storage,  value: app:os_env }
      - { name: process_host, value: app:processes }
```
Set API keys as env vars (`OPENAI_API_KEY`, `ANTHROPIC_API_KEY`, …) — exposed via `env.storage.os`.

### Text generation
```yaml
entries:
  - name: ask
    kind: function.lua
    source: file://ask.lua
    method: handler
    imports: { llm: wippy.llm:llm }
```
```lua
local llm = require("llm")
local function handler()
    local response, err = llm.generate("What are the three laws of robotics?", { model = "gpt-4o" })
    if err then return nil, err end
    return response.result
end
return { handler = handler }
```

First arg to `generate()`: a **string**, a **prompt builder**, or a **table of messages**. Second arg
is options.

**Generate options:** `model` (required), `temperature` (0–1), `max_tokens`, `top_p`, `top_k`,
`thinking_effort` (0–100, thinking-capable models), `tools` (array of tool defs),
`tool_choice` (`"auto"|"none"|"any"|<tool name>`), `stream` (`{ reply_to, topic, buffer_size }`),
`timeout` (seconds, default 600).

**Response:** `result` (text), `tokens` (`prompt_tokens`, `completion_tokens`, `thinking_tokens`,
`total_tokens`, optional cache fields), `finish_reason` (`"stop"|"length"|"tool_call"|"filtered"|"error"`),
`tool_calls?`, `metadata`, `usage_record?`.

### Prompt builder (multi-turn)
```yaml
imports: { llm: wippy.llm:llm, prompt: wippy.llm:prompt }
```
```lua
local llm, prompt = require("llm"), require("prompt")
local conv = prompt.new()
conv:add_system("You are a helpful assistant.")
conv:add_user("What is the capital of France?")
local response, err = llm.generate(conv, { model = "gpt-4o", temperature = 0.7, max_tokens = 500 })
```

Other `wippy/llm` capabilities: structured output (force JSON schema), embeddings (`llm.embed`),
and streaming (via the `stream` option → messages delivered to `reply_to`/`topic`).

---

## wippy/agent — declarative AI agents

Build agents with tool use, streaming, delegation, traits, and memory. Agents are **declarative
registry entries** executed via a **context/runner** pattern.

### Setup
```bash
wippy add wippy/agent
wippy install
```
Requires `wippy/llm` + a `process.host`. Declare both as `ns.dependency` (agent dep takes a
`process_host` parameter).

### Agent definition (`meta.type: agent.gen1`)
```yaml
entries:
  - name: assistant
    kind: registry.entry
    meta:
      type: agent.gen1
      name: assistant
      title: Assistant
      comment: A helpful chat assistant
    prompt: |
      You are a helpful assistant. Be concise and direct.
    model: gpt-4o
    max_tokens: 1024
    temperature: 0.7
    tools:        [ app.tools:search, app.tools:query_db ]   # tool registry IDs
    traits:       [ conversational ]                          # trait references
    delegates:    [ app.agents:specialist ]                  # delegate agent references
    memory:       [ "Always cite sources." ]                 # static memory items
```

**Agent fields:** `meta.type` (must be `agent.gen1`), `meta.name`, `prompt` (system prompt),
`model`, `max_tokens` (default 512), `temperature` (default 0), `thinking_effort` (forwarded when > 0),
`tools[]`, `traits[]`, `delegates[]`, `memory[]` (strings), `memory_contract` (dynamic memory config).

> **Tools** are `function.lua` entries with `meta.type: tool` and a `meta.input_schema` (JSON schema)
> describing their arguments — the agent calls them by registry ID.

### Running an agent (context/runner)
```yaml
imports: { agent_context: wippy.agent:context }
```
```lua
local agent_context = require("agent_context")

local ctx = agent_context.new({
    context = { session_id = "abc", user_id = "u1" },
    enable_cache = true,                       -- prompt cache markers (Claude); default true
})

local runner, err = ctx:load_agent("app:assistant")
if err then error("load agent: " .. tostring(err)) end
-- runner drives the agent turn(s); feed user input and read responses/tool calls
```

**Context methods:** `agent_context.new(options?)`, `:add_tools(specs)`, `:add_delegates(specs)`,
`:configure_delegate_tools(cfg)`, `:set_memory_contract(cfg)`, `:set_context_merger(fn)`,
`:update_context(updates)`, `:load_agent(spec_or_id, options?)`, `:switch_to_agent(id, options?)`,
`:switch_to_model(name)`, `:get_current_agent()`, `:get_config()`.

**Inline spec** (no registry entry): pass a table to `:load_agent({ id=..., name=..., prompt=...,
model=..., max_tokens=... })`.

---

## wippy/test — BDD testing

See LUA-PATTERNS → Testing for the full API. Tests are `function.lua` with `meta.type: test`;
`describe/it` + assertions + lifecycle hooks + mocking; run with `wippy run test [pattern...]`.

---

## Other framework modules (pointers)

- **wippy/dataflow** — DAG-based orchestration (function, agent, cycle, parallel nodes). Doc:
  `framework/dataflow`.
- **wippy/relay** — WebSocket relay with a central hub, per-user hubs, plugin routing. Doc:
  `framework/relay`.
- **wippy/views** — page & component system with template rendering. Doc: `framework/views`.
- **wippy/facade** — frontend iframe facade with authentication bridging. Doc: `framework/facade`.
- **embeddings / migrations / usage** — `framework/embeddings`, `framework/migration`, `framework/usage`.

Fetch any of these live: `https://wippy.ai/llm/path/en/framework/<name>`.
