# IDE Introspector — MCP Tool Reference & Recipes

The **IDE Introspector** (`com.github.xepozz.ide.introspector`, repo
`j-plugins/ide-introspector-plugin`) is an **IntelliJ plugin** that exposes the *running* IDE to MCP
clients. Its tools register via the `com.intellij.mcpServer.mcpToolset` extension point and surface
through the **JetBrains MCP Server** — in this Claude session that is the **`idea` MCP server**.

## Setup / prerequisites

1. Install & enable the **IDE Introspector** plugin in the target IDE (not on Marketplace yet —
   build from source with `./gradlew buildPlugin` → install the zip from disk, then restart).
2. Enable the bundled **MCP Server** plugin (this is what publishes the tools to MCP clients).
3. Tools then appear to MCP clients as `arch__list_plugins`, `ui__get_tree`, `psi__get_structure`,
   `exec__execute_kotlin_in_ide`, etc. (group prefix + `__` + tool).
4. Reflects the **live** IDE: disabled plugins, dev builds, custom installs are all accurate.

## Two tiers

| Tier | What | When |
|------|------|------|
| **Tier 1** — 36 pre-built read-only tools (`arch/code/context/editor/events/ide/psi/screenshot/services/skill/ui`) | Fast (1–50 ms), whitelisted | Default for all routine introspection |
| **Tier 2** — `exec.execute_kotlin_in_ide` | Compiles & runs Kotlin in the IDE JVM (1–5 s) | Only when no Tier-1 tool fits |

`exec` is **off by default**, requires per-call confirmation in the IDE, blocks
`Runtime.exec`/`ProcessBuilder`/`setAccessible(true)`/`System.exit`/`Class.forName("sun.*")`, audits to
`idea.log`, and is hard-capped at 10 s. **Always prefer a Tier-1 tool** — it's ~100× faster.

---

## Tool catalog (37 tools)

### `arch.*` — plugin & extension-point architecture (live)
- **`arch.list_extension_points`** — enumerate EPs. Filter with `nameContains` (1000+ on vanilla IDEA),
  `area` (`application`/`project`/`both`), `declaredByPlugin`, `onlyDynamic`.
- **`arch.list_extensions_for_ep`** — every extension registered against one EP
  (`extensionPointName="com.intellij.toolWindow"`). Returns impl class, providing plugin, XML attrs.
- **`arch.find_extenders_of`** — reverse lookup "who implements X?" by EP name **or** class
  (`targetKind="auto"` tries EP then class scan).
- **`arch.list_plugins`** — installed plugins (id, version, vendor, bundled/enabled, deps, EP/ext counts).
  `includeBundled=false` to focus on third-party; `nameOrIdContains` to find an id.
- **`arch.get_plugin_details`** — full inventory of one plugin: metadata, declared EPs, contributed
  extensions, services, listeners, topics, optional actions. (`includeActions` is slow — off by default.)

### `services.*` — declared services
- **`services.list`** — `<applicationService>`/`<projectService>`/`<moduleService>` across plugins;
  filter by `area`, `providedByPlugin`, `interfaceContains`, `implementationContains`.
- **`services.find`** — reverse lookup by interface/impl FQCN (`targetKind=auto|interface|implementation`).

### `events.*` — message bus
- **`events.list_listeners`** — declarative `<applicationListeners>`/`<projectListeners>` pairs.
- **`events.list_topics`** — `Topic<L>` declarations (bytecode scan; default non-bundled only — pass
  `providedByPlugin` for a fast single-plugin scan, `includeBundled=true` is slow).
- **`events.find_listeners_of_topic`** — listeners for a given Topic FQCN.

### `code.*` — class source & API (project scope)
- **`code.find_class`** — cheap probe: resolves an FQCN and reports availability
  (`SOURCE`/`ATTACHED_SOURCE`/`DECOMPILED`/`STUBS_ONLY`/`NOT_FOUND`) without the text.
- **`code.get_source`** — best-available source text (real > attached > decompiled > stubs), `maxBytes` cap.
- **`code.list_members`** — methods/constructors/fields/inner classes with signatures (cheap vs full source).
- **`code.attach_sources`** — trigger the IDE's "download sources" action for a library class (async).

### `psi.*` — PSI of open files (project scope)
- **`psi.list_open_files`** — open editor tabs + the focused one (cheap; metadata only).
- **`psi.get_structure`** — full PSI tree of a file (all language roots + injections), `maxNodes` cap.
- **`psi.get_references`** — references in a file or at a position (`scope="file"|"at_offset"`), resolved.
- **`psi.find_usages`** — all usages of the declaration at a position (Find Usages), `scope`/overrides.

### `ui.*` — live Swing UI (inspect + interact)
- **`ui.get_tree`** — live component tree (BFS). Scope with `rootSelector` (`"frame"`, `"dialog"`,
  `"tool_window:Project"`), `maxDepth` (≤15). Stable `c_xxxxxxxx` ids for follow-ups.
- **`ui.find_by_name`** — locate by `name`/`text`/`accessibleName`/`toolTipText`/`className`,
  `matchMode=exact|contains|regex`.
- **`ui.find_by_xpath`** — robot-compatible XPath subset, e.g.
  `//div[@class='ActionButton' and @text='Run']`.
- **`ui.find_by_coordinates`** — deepest component at a screen/frame pixel.
- **`ui.get_properties`** — full property bag for a located component id (bounds, a11y, client props,
  UI-Inspector beans like `uiInspector[ActionId]`).
- **`ui.list_items`** — logical items inside a JTree/JList/JTable/JTabbedPane/JComboBox (not separate
  components — reads the widget model).
- **`ui.select_item`** / **`ui.activate`** — select (single-click) vs activate (double-click/open) an
  item by `index`/`text`/`path`.
- **`ui.click`** — press a button/checkbox (`doClick()`) or raw click at a local point.

> Component ids are **session-stable** — pass them between `ui.*` and `screenshot.capture`. They die on
> IDE restart or when the panel closes.

### `screenshot.*` — visual capture
- **`screenshot.capture`** — PNG of `component` (needs a `componentId`), `active_frame`, `all_frames`,
  or `screen` (only `screen` includes popups/tooltips). Returns a temp file path. Use `scale` to shrink.
- **`screenshot.crop`** — rectangular crop of the active frame or virtual desktop.

### `editor.*` / `ide.*` — editor & index state
- **`editor.get_active`** — focused editor: file, caret (1-based), selection.
- **`editor.list_tabs`** — open editor tabs grouped by split window; marks selected/active.
- **`ide.indexing_status`** — dumb (indexing) vs smart (ready) for the focused project. Poll before
  trusting symbol resolution / `psi.*` / find-usages.

### `context.*` / `skill.*` — bundled IntelliJ-platform knowledge corpus
- **`context.search`** → ranked locators; **`context.get`** → one entry body (paginated).
- **`skill.list`** → curated skill cards by tag; **`skill.get`** → one skill body. Good for retrieving
  the plugin's own bundled playbooks ("declaring a service", "the extension point model", …).

### `exec.*` — Tier 2 escape hatch
- **`exec.execute_kotlin_in_ide`** — compile+run Kotlin. Implicit bindings in scope: `project: Project?`,
  `pluginDisposable: Disposable`, `read { }` (ReadAction), `write { }` (WriteCommandAction),
  `onEdt { }` (invokeAndWait). Last expression → JSON. Return primitives/Strings/Maps (IntelliJ API
  objects fall back to `.toString()`). `runOn="edt"` (default, for Swing) or `"background"`.

---

## Task → tool recipes

| Goal | Tools |
|------|-------|
| "Does an EP for X exist? Who uses it?" | `arch.list_extension_points nameContains="X"` → `arch.list_extensions_for_ep` |
| "Who implements interface/EP Y?" | `arch.find_extenders_of target="Y"` |
| "What does plugin Z register?" | `arch.list_plugins nameOrIdContains="Z"` → `arch.get_plugin_details pluginId="…"` |
| "Is there a service for interface I?" | `services.find target="I"` (or `services.list interfaceContains="I"`) |
| "Who reacts to file-editor events?" | `events.list_listeners topicContains="FileEditorManager"` |
| "What does class C expose? Show source." | `code.find_class fqn="C"` → `code.list_members` / `code.get_source` |
| "Map the PSI of the open file / find usages" | `psi.list_open_files` → `psi.get_structure` / `psi.find_usages` |
| "Find/press the Run button" | `ui.find_by_xpath "//div[@class='ActionButton' and @text='Run']"` → `ui.click` |
| "Screenshot the Project tool window" | `ui.get_tree rootSelector="tool_window:Project"` → `screenshot.capture target="component" componentId="…"` |
| "Is the index ready before I trust resolution?" | `ide.indexing_status` |
| "Something no pre-built tool covers" | `exec.execute_kotlin_in_ide` (confirm it's enabled in Settings) |

### Example: verify your own plugin loaded in the sandbox after `runIde`

1. `arch.list_plugins nameOrIdContains="<your-plugin>"` → confirm enabled + version.
2. `arch.get_plugin_details pluginId="<your-id>"` → confirm your EPs/extensions/services registered.
3. `ui.get_tree rootSelector="tool_window:<YourToolWindow>"` → confirm UI is mounted.
4. If a registration seems missing, `events.list_listeners providedByPlugin="<your-id>"` /
   `services.list providedByPlugin="<your-id>"` to see exactly what the platform picked up.

### Example: one-off live check with exec (when no Tier-1 tool fits)

```kotlin
// list every tool window id in the focused project
com.intellij.openapi.wm.ToolWindowManager.getInstance(project!!).toolWindowIds.toList()
```

```kotlin
// path of the file in the active editor (PSI/VFS read → wrap in read{})
read {
  com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project!!)
    .selectedTextEditor?.virtualFile?.path
}
```

Keep `exec` snippets short and read-only; for anything routine, a Tier-1 tool is faster and safer.
