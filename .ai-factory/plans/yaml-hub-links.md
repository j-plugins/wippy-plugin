# Implementation Plan: Hub links for YAML registry components

Branch: main
Created: 2026-06-29

## Settings
- Testing: no
- Logging: verbose
- Docs: no  # WARN [docs] only, no mandatory docs checkpoint

## Roadmap Linkage
Milestone: "none"
Rationale: No ROADMAP.md exists in this project; linkage not applicable.

## Goal

Add clickable links from Wippy YAML registry files (`_index.yaml`) to the public
Wippy hub at `https://hub.wippy.ai`, so a developer can jump from a dependency
declaration to the corresponding module page.

## Research Summary & Key Decisions

### Hub URL scheme (confirmed against live pages)
- Pattern: `https://hub.wippy.ai/{org}/{module}` (two GitHub-style segments).
- Version pinning is a query param: `?version=0.4.3`.
- The module page is the deepest addressable unit — **individual entries have no
  own hub URL**.

### What in YAML maps to a hub module (decided scope)
Real example files (`playground/examples/*/src/_index.yaml`) show that a file's
own `namespace:` is local (e.g. `app`) and does **not** exist on the hub. The
explicit, unambiguous hub reference is the `component:` field of a dependency
entry:

```yaml
- name: __dependency.llm
  kind: ns.dependency
  component: wippy/llm      # already in org/module form
  version: "*"
```

**Scope (user decision): only `component:` inside `kind: ns.dependency` entries.**
The value is already `org/module`, so no namespace-splitting heuristics are
needed. Other reference sites (`imports:`, `providers[].id`) are intentionally
out of scope for this iteration.

### URL construction rules
- URL = `https://hub.wippy.ai/{component}` where `component` is the raw
  `component:` value, validated to be exactly two non-empty slash-separated
  segments (skip otherwise).
- Version (user decision): append `?version=<version>` only when the sibling
  `version:` on the same entry is non-blank and `!= "*"`; otherwise link to the
  latest module page.

### Mechanism (user decision)
Two complementary surfaces, both reusing one shared URL helper:
1. **Gutter icon** — a `LineMarkerProvider` on the `component:` line
   (`AllIcons.Ide.External_link_arrow`), click opens the browser. Mirrors the
   existing `reference/WippySourceLineMarkerProvider`.
2. **Intention action** — appears in the Alt+Enter / Cmd+Enter "show
   intentions / quick actions" popup when the caret is on a `component:` value.

`WebReference` / Ctrl+Click was considered and rejected for this iteration.
Browser is opened via the canonical `com.intellij.ide.BrowserUtil.browse(url)`.

### Architecture placement
New self-contained vertical slice `com.github.xepozz.wippy.hub` (one slice per
feature). Extensions are registered in `META-INF/language-yaml.xml` (they depend
on `org.jetbrains.plugins.yaml`), next to the existing YAML line-marker
registrations. User-facing strings go through `MyBundle`.

## Commit Plan
- **Commit 1** (after tasks 1-3): `feat(hub): add hub URL helper, line marker, and intention for ns.dependency`
- **Commit 2** (after task 4): `feat(hub): register hub link extensions and bundle strings`
- (Task 5 is manual verification — no commit; fix-ups fold into the relevant commit.)

## Tasks

### Phase 1: Core logic
- [x] Task 1: Add `hub/` slice with `WippyHubUrl` helper — pure URL builder
  (`buildHubUrl(component, version)`, org/module validation, `?version=` rule)
  plus a PSI helper that detects the `component:` value of a `ns.dependency`
  entry and returns `(component, version)`. Gated on `isInWippyDefinitionFile()`.
  Single source of truth reused by tasks 2 and 3. Verbose logging.

### Phase 2: UI surfaces
- [x] Task 2: Add `WippyHubLineMarkerProvider` — gutter icon on the `component:`
  scalar, `AllIcons.Ide.External_link_arrow`, click → `BrowserUtil.browse(url)`.
  Mirrors `WippySourceLineMarkerProvider`. (depends on 1)
- [x] Task 3: Add `WippyOpenHubIntention` — `IntentionAction` available when the
  caret is on a valid `component:` value; `invoke` opens the browser;
  `startInWriteAction() = false`. (depends on 1)
<!-- Commit checkpoint: tasks 1-3 -->

### Phase 3: Wiring
- [x] Task 4: Register the line-marker and `<intentionAction>` in
  `language-yaml.xml`; add `hub.*` keys to `MyBundle.properties` and use them in
  tasks 2-3 (no hardcoded strings). (depends on 2, 3)
<!-- Commit checkpoint: task 4 -->

### Phase 4: Verification
- [x] Task 5: Manually verify in the sandbox IDE (`buildPlugin` + `runIde`)
  against `playground/examples/20-bar-roguelike/src/_index.yaml`: gutter icon and
  intention open the correct `https://hub.wippy.ai/wippy/{llm,views}` URLs; no
  link on non-dependency lines; verify the `?version=` path with a temporary
  concrete version. (depends on 4)

## Files
- `src/main/kotlin/com/github/xepozz/wippy/hub/WippyHubUrl.kt` (new)
- `src/main/kotlin/com/github/xepozz/wippy/hub/WippyHubLineMarkerProvider.kt` (new)
- `src/main/kotlin/com/github/xepozz/wippy/hub/WippyOpenHubIntention.kt` (new)
- `src/main/resources/META-INF/language-yaml.xml` (edit)
- `MyBundle.properties` (edit — resolve actual path)

## Reference patterns in the codebase
- Line marker on a YAML scalar: `reference/WippySourceLineMarkerProvider.kt`
- Intention/quick-fix base usage: `reference/CreateWippyLuaFileFix.kt`
- Wippy-file gate helper: `util/WippyExtensions.kt` → `isInWippyDefinitionFile()`
- Existing YAML registrations: `META-INF/language-yaml.xml`
