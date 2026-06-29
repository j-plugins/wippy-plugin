# Wippy Lua Patterns & Module APIs — Reference

Canonical APIs and examples for writing Wippy Lua. Globals (no `require`, no `modules:` entry):
**`process`**, **`channel`**, **`errors`**. Everything else must be in `modules:` or `imports:`.

---

## 1. Functions (`function.lua`)

Stateless; receive args, return `result, err`. The file returns the handler (or a table of methods;
`method:` selects one).

```lua
local sql = require("sql")
local function get_user(id)
    local db, err = sql.get("app:main_db")
    if err then return nil, err end
    local rows, err = db:query("SELECT * FROM users WHERE id = $1", id)
    if err then return nil, err end
    if #rows == 0 then return nil, errors.new({ kind = errors.NOT_FOUND, message = "not found" }) end
    return rows[1]
end
return get_user
```

### Calling functions (`funcs`, `modules: [funcs]`)

```lua
local funcs = require("funcs")
local result, err = funcs.call("namespace:function_name", arg1, arg2)   -- sync

local future = funcs.async("namespace:function_name", arg1)             -- async → Future
local result, err = future:result()

local exec = funcs.new():with_context({ user_id = "123" })              -- attach context
exec:call("namespace:function_name")
```

`funcs.call` propagates context (trace IDs, user, security) and, in workflows, is recorded for
deterministic replay — prefer it over reaching across entries by other means.

---

## 2. Errors (global `errors`)

```lua
local err = errors.new("something went wrong")                  -- kind defaults to UNKNOWN
local err = errors.new({
    message = "user not found",
    kind = errors.NOT_FOUND,
    retryable = false,
    details = { user_id = 123 },
})
-- NOTE: the (kind, message) two-arg form is NOT supported — pass a string or a table.

if err then
    if errors.is(err, errors.NOT_FOUND) then --[[ handle ]] end
    return nil, errors.wrap(err, "context message")
end
```

**Kinds:** `UNKNOWN, INVALID, NOT_FOUND, ALREADY_EXISTS, PERMISSION_DENIED, TIMEOUT, CANCELED,
UNAVAILABLE, INTERNAL, CONFLICT, RATE_LIMITED`. Errors carry `kind`, `message`, `retryable`,
`details`. Available everywhere without `require`.

---

## 3. Processes (`process.lua`, global `process`)

Actors with a PID and inbox; keep state across messages. The `process` global is always available.

### Identity, spawn, control
```lua
local pid = process.pid()      -- this process's PID
local fid = process.id()       -- call-chain / frame id

local pid, err = process.spawn(id, host, ...)                 -- e.g. ("app.workers:task","app:processes",cfg)
local pid, err = process.spawn_monitored(id, host, ...)       -- get EXIT events
local pid, err = process.spawn_linked(id, host, ...)          -- get LINK_DOWN on abnormal exit
local result, err = process.exec(id, host, ...)               -- spawn, wait, return result

process.send(pid, "topic", data)                              -- fire-and-forget (topic must not start with @)
process.terminate(pid)                                        -- force kill
process.cancel(pid, "shutting down")                          -- graceful cancel
```

### Inbox, events, the actor loop
```lua
local function worker(config)
    local inbox  = process.inbox()    -- Message objects (@inbox)
    local events = process.events()   -- lifecycle events (@events)
    while true do
        local r = channel.select { inbox:case_receive(), events:case_receive() }
        if r.channel == events then
            local ev = r.value
            if ev.kind == process.event.CANCEL then break end       -- also EXIT, LINK_DOWN
        else
            local msg   = r.value
            local topic = msg:topic()
            local data  = msg:payload():data()
            handle(topic, data)
        end
    end
end
return worker
```

Event kinds: `process.event.CANCEL`, `process.event.EXIT`, `process.event.LINK_DOWN`.
EXIT/LINK_DOWN events carry `from` (PID) and `result = { value, error }`.

### Monitoring, linking, naming
```lua
process.monitor(pid) ; process.unmonitor(pid)         -- EXIT notifications
process.link(pid)    ; process.unlink(pid)            -- bidirectional LINK_DOWN
process.set_options({ trap_links = true })            -- deliver LINK_DOWN to events channel

process.registry.register("worker-1")                 -- register self under a name (LOCAL by default)
local pid, err = process.registry.lookup("worker-1")  -- any destination arg accepts a name
process.registry.unregister("worker-1")
```
Name scopes (cluster): `process.registry.LOCAL | EVENTUAL | CONSISTENT | STRONG`.

### Custom topics & context spawner
```lua
local ch = process.listen("my_topic", { message = true })   -- subscribe to a topic
local spawner = process.with_context({ request_id = "123" })
spawner:with_name("w1"):with_message("init", cfg):spawn(id, host)
```

> **Supervision = "let it crash":** spawn workers monitored/linked, react to EXIT/LINK_DOWN, restart
> as policy dictates. Don't defensively swallow everything — design the supervisor.

---

## 4. Channels (global `channel`)

Go-style channels for concurrency between coroutines/processes.

```lua
local sync = channel.new()        -- unbuffered: sender+receiver rendezvous
local q    = channel.new(10)      -- buffered: send completes while space remains

q:send(value)                     -- raises if closed
local val, ok = q:receive()       -- val,true | nil,false (closed & empty)
q:close()                         -- pending receivers get nil,false

-- select on multiple operations
local r = channel.select {
    a:case_receive(),
    b:case_send(value),
    default = true,               -- optional: return immediately if none ready
}
-- r = { channel, value, ok }  for a case; r = { default = true } for the default branch
```

### Patterns
```lua
-- timeout
local time = require("time")
local r = channel.select { work:case_receive(), time.after("5s"):case_receive() }
if r.channel ~= work then return nil, errors.new({ kind = errors.TIMEOUT, message = "timed out" }) end

-- fan-in: multiplex inbox + events + shutdown   (see Processes loop above)
-- worker pool: feed a buffered `work` channel, collect from a `results` channel, close to signal done
```

---

## 5. Workflows (`workflow.lua`)

Durable; survive crashes. Write normal Lua; the runtime records `funcs.call`, `time.sleep`, random,
`time.now`, `uuid.v4` for deterministic replay. **Do not call non-deterministic external APIs directly.**

```lua
local function order_flow(order)
    local inv = funcs.call("app:reserve_inventory", order.items)
    if not inv then return nil, errors.new("out of stock") end

    local pay = funcs.call("app:charge_payment", order.total)
    if not pay then
        funcs.call("app:release_inventory", inv.id)
        return nil, errors.new("payment failed")
    end

    local msg = process.inbox():receive()           -- can block for days, durably
    if not msg:payload():data().approved then
        funcs.call("app:refund_payment", pay.id)
        funcs.call("app:release_inventory", inv.id)
        return nil, errors.new("rejected")
    end
    return funcs.call("app:fulfill_order", order.id)
end
return order_flow
```

---

## 6. Data access

### SQL (`modules: [sql]`)
```lua
local sql = require("sql")
local db, err = sql.get("app:main_db")
local rows, err = db:query("SELECT * FROM users WHERE active = $1", true)   -- $1,$2 placeholders
local ok, err   = db:execute("INSERT INTO users (name) VALUES ($1)", name)
```

### Key-value store (`modules: [store]`)
```lua
local store = require("store")
local cache = store.get("app:cache")
cache:set("key", value, 3600)        -- TTL seconds
local val = cache:get("key")
```

### Queue (`modules: [queue]`)
```lua
local queue = require("queue")
queue.publish("app:tasks", { task = "process", id = 123 })
```

### Filesystem (`modules: [fs]`)
```lua
local fs = require("fs")
local vol = fs.get("app:storage")
local data = vol:readfile("path/to/file.txt")
vol:writefile("output.txt", content)
```

---

## 7. HTTP

### Handler (in a `function.lua` wired to an `http.endpoint`, `modules: [http, json]`)
```lua
local http = require("http")
local function handler()
    local req = http.request()
    local res = http.response()
    local id  = req:param("id")
    local user, err = funcs.call("app.api:get_user", id)
    if err then res:set_status(404) ; res:write_json({ error = err:message() }) ; return end
    res:write_json(user)
end
return handler
```

### Outbound HTTP (`modules: [http_client]`)
```lua
local http_client = require("http_client")
local resp, err = http_client.get("https://api.example.com/data", {
    headers = { Authorization = "Bearer token" },
    timeout = "10s",
})
local body = resp.body
```

---

## 8. Security (`modules: [security]`)
```lua
local security = require("security")
local actor   = security.actor()                       -- who is calling
local scope   = security.scope()                       -- permissions in effect
local allowed = security.can("read", "resource:users") -- permission check

local ts    = security.token_store("app:tokens")
local token = ts:create(actor, scope, { expiration = "24h" })
local a, s  = ts:validate(token)
```
Process operations are authorized by permissions (`process.spawn`, `process.send`, `process.host`,
`process.registry.register`, …) checked against the caller's actor.

---

## 9. Time (`modules: [time]`)
```lua
local time = require("time")
time.sleep("5s")
local now    = time.now()
local once   = time.after("30s")    -- channel that fires once
local ticker = time.ticker("10s")   -- repeating channel
```

---

## 10. Events (`modules: [events]`)
```lua
local events = require("events")
events.send("orders", "order.created", "/orders/123", { order_id = "123" })   -- publish

local sub = events.subscribe("orders.*")    -- wildcards supported
local ch  = sub:channel()
local evt = ch:receive()
```

---

## 11. Testing (`wippy/test`, `imports: { test: wippy.test:test }`)

Tests are `function.lua` entries with `meta.type: test`.

```lua
local test = require("test")
local function define()
    test.describe("calculator", function()
        test.before_each(function() --[[ setup ]] end)
        test.it("adds", function() test.eq(1 + 1, 2) end)
        test.it("errors on bad input", function()
            local _, err = validate({})
            test.not_nil(err)
        end)
    end)
end
local run_cases = test.run_cases(define)
return { run = function(opts)
    local r = run_cases(opts)
    if r.failed_tests > 0 then error("tests failed: " .. r.failed_tests) end
    return r
end }
```

Assertions: `eq/neq, ok/fail, is_nil/not_nil, is_true/is_false, is_string/number/table/function/boolean,
contains/matches/has_key/len, gt/gte/lt/lte, throws/has_error/no_error`. Hooks:
`before_all/after_all/before_each/after_each`. Mocks: `test.mock("process.send", fn)` (auto-restored).
Run: `wippy run test [pattern...]`. The runner needs a `process.host` + `terminal.host`.
