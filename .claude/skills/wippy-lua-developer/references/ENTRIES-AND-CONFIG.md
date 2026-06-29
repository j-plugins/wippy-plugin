# Wippy Registry, Entries & Configuration — Reference

Source: wippy.ai docs (`guides/entry-kinds`, `lua/entries`, `start/structure`, `concepts/registry`).
Live docs: `https://wippy.ai/llm/path/en/<path>`.

## The registry

Everything in Wippy is a **registry entry**: `{ id: "namespace:name", kind, meta, data }`.

- The **kind** determines behavior (function, process, http endpoint, db connection, agent, …).
- YAML `_index.yaml` files are one way to **declare** entries; the registry is the **runtime source
  of truth** and entries can be created/updated/deleted **while the system runs** (live, no redeploy).
- Entries reference each other by `namespace:name`. The registry **auto-wires dependencies** and
  initialization order from those references.

Manipulate the registry from Lua (`modules: [registry]`):
```lua
local registry = require("registry")
local entry = registry.get("app.api:get_user")
local tests = registry.find({ ["meta.type"] = "test" })   -- query by metadata

-- create/modify entries at runtime via a snapshot + changeset
local snap = registry.snapshot()
local changes = snap:changes()
changes:create({ id = "app:new_func", kind = "function.lua", data = { ... } })
changes:apply()
```

## Project structure

```
myapp/
├── .wippy.yaml          # runtime configuration
├── wippy.lock           # source directories (what the loader scans)
└── src/
    ├── _index.yaml      # entries for namespace: app
    ├── api/
    │   ├── _index.yaml  # namespace: app.api
    │   └── handler.lua
    └── workers/
        ├── _index.yaml  # namespace: app.workers
        └── task.lua
```

`.wippy.yaml`, `wippy.lock`, or a `.wippy/` directory mark a Wippy project (the IDE plugin keys
language/LSP activation off these).

## `_index.yaml` shape

```yaml
version: "1.0"
namespace: app.api          # all entries below get this namespace
entries:
  - name: get_user          # full id = app.api:get_user
    kind: function.lua
    source: file://handler.lua   # relative file with the Lua source
    method: get_user             # which returned function/method to invoke
    modules: [sql, json]         # capabilities this entry may require()
    meta:                        # arbitrary metadata, queryable via registry.find
      type: tool                 # e.g. expose function as an AI tool
```

### Common entry fields

| Field | Meaning |
|-------|---------|
| `name` | local name; combined with `namespace` → `namespace:name` |
| `kind` | entry kind (behavior) |
| `source` | `file://<relative.lua>` source location (for `*.lua` kinds) |
| `method` | the function/method to call (the Lua file returns it, or a table of them) |
| `modules` | list of module names the entry may `require()` (capability grant) |
| `imports` | map alias → entry id, to `require("alias")` another registry library/module |
| `meta` | metadata (`type`, `router`, `suite`, `input_schema`, …); queryable |
| `lifecycle` | e.g. `auto_start: true` for hosts/services |

### `modules:` vs `imports:`

- **`modules:`** grants built-in runtime modules: `sql, json, http, http_client, time, funcs, store,
  queue, fs, security, registry, events, uuid, yaml, base64, logger, ...`. (Globals `process`,
  `channel`, `errors` need no grant.)
- **`imports:`** wires **another registry entry** (a framework library or your own `library.lua`)
  under an alias:
  ```yaml
  imports:
    llm: wippy.llm:llm
    prompt: wippy.llm:prompt
    test: wippy.test:test
  ```
  then `local llm = require("llm")`.

## Module access control & determinism

Each entry can `require()` **only** the modules it declares. Modules not listed are **unavailable** —
there is no `os.execute`, `io.open`, `debug.*`, or `package.*` unless explicitly granted. The runtime
controls access at the **module level** (it does not scan source).

This is also how **workflow determinism** works: `workflow.lua` entries receive only deterministic
modules; the runtime intercepts `time.now()`, `uuid.v4()`, etc. and records results for replay.

## Entry kinds (common)

| Kind | Purpose |
|------|---------|
| `function.lua` | Stateless callable (also AI tool / HTTP handler) |
| `library.lua` | Reusable Lua library imported by other entries |
| `process.lua` | Long-running actor |
| `workflow.lua` | Durable Temporal-backed workflow |
| `http.service` | HTTP server |
| `http.router` | Route group with middleware |
| `http.endpoint` | URL+method → `func: ns:handler` |
| `http.static` | Static file serving |
| `db.sql.postgres` / `db.sql.mysql` / `db.sql.sqlite` | Database connection |
| `store.memory` / `store.sql` | Key-value store |
| `queue.queue` | Message queue |
| `process.host` | Host that runs processes (`lifecycle.auto_start: true`) |
| `process.service` | Supervised process |
| `terminal.host` | Terminal host (needed by the test runner) |
| `env.storage.os` | Exposes OS env vars (e.g. API keys) to other modules |
| `ns.dependency` | Declares a dependency on a framework component (`wippy/llm`, `wippy/agent`, …) |
| `contract.definition` / `contract.binding` | Typed service interface + implementation |
| `registry.entry` | Plain configuration data / declarative defs (e.g. `agent.gen1`) |

`meta.type` refines behavior: `tool` (expose a function as an AI tool, with `meta.input_schema`
JSON schema), `test` (discoverable by `wippy run test`), `agent.gen1` (declarative agent).

## HTTP entries (wiring a route)

```yaml
version: "1.0"
namespace: app
entries:
  - name: server
    kind: http.service
    lifecycle: { auto_start: true }

  - name: api_router
    kind: http.router
    meta: { server: app:server }
    # middleware (cors, auth, logging, rate-limit) attaches here

  - name: get_user.endpoint
    kind: http.endpoint
    meta: { router: app:api_router }
    method: GET
    path: /users/{id}
    func: app.api:get_user      # the function.lua that handles it
```

Handler reads request / writes response via the `http` module (see LUA-PATTERNS → HTTP).

## Dependencies on framework modules

Framework modules (`wippy/llm`, `wippy/agent`, `wippy/test`, `wippy/dataflow`, `wippy/relay`,
`wippy/views`, `wippy/facade`) are added with the CLI and declared as `ns.dependency` entries:

```bash
wippy add wippy/llm
wippy install
```

```yaml
entries:
  - name: os_env
    kind: env.storage.os
  - name: processes
    kind: process.host
    lifecycle: { auto_start: true }
  - name: dep.llm
    kind: ns.dependency
    component: wippy/llm
    version: "*"
    parameters:
      - { name: env_storage, value: app:os_env }
      - { name: process_host, value: app:processes }
```
