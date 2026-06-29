# Architecture: Structured Modules (Vertical Slices)

## Overview

This plugin is built on the **IntelliJ Platform** extension model and organized as
**feature-oriented vertical slices**: each top-level package under
`com.github.xepozz.wippy` owns a cohesive capability (the WippyLua language, LSP
integration, the registry index, project detection, run configurations, the SDK, etc.)
and contains everything that capability needs — file types, PSI, services, listeners,
actions, and settings.

The plugin does not invent its own runtime layering; the IntelliJ Platform supplies the
"framework" (extension points, services, listeners, the PSI/VFS model). Architecture here
is mostly about **how features are partitioned into packages** and **how they declare
themselves to the platform** via `plugin.xml`. Vertical slices keep each feature
self-contained and independently understandable, which suits a single-maintainer plugin
that grows one capability at a time.

## Decision Rationale

- **Project type:** IntelliJ Platform plugin (IDE tooling), not a web/service backend.
- **Tech stack:** Kotlin (JVM 21) + generated Java PSI/parser/lexer (Grammar-Kit), IntelliJ Platform 2025.1.x, LSP4IJ.
- **Key factor:** The codebase is already partitioned by feature package; the platform owns lifecycle and wiring, so the meaningful unit of design is the feature slice, not a technical layer.

## Folder Structure

```
src/
├── main/
│   ├── kotlin/com/github/xepozz/wippy/
│   │   ├── WippyFileType.kt, WippyIcons.kt, MyBundle.kt   # plugin-wide primitives
│   │   ├── lang/            # ── SLICE: WippyLua language ──
│   │   │   ├── WippyLuaLanguage.kt, WippyLuaFileType.kt
│   │   │   ├── WippyLuaParserDefinition.kt, WippyLuaLexerAdapter.kt
│   │   │   ├── WippyLuaSyntaxHighlighter*.kt, WippyAnnotator.kt
│   │   │   ├── WippyLuaFoldingBuilder.kt, WippyLuaBraceMatcher.kt, WippyLuaCommenter.kt
│   │   │   └── psi/         # hand-written PSI helpers (impl in src/main/gen)
│   │   ├── lsp/             # ── SLICE: LSP4IJ integration ──
│   │   │   ├── WippySocketLspServerSupportProvider.kt, WippySocketLspServerDescriptor.kt
│   │   │   ├── WippyLspConnectionService.kt, WippyLspSettings.kt, WippyLspConfigurable.kt
│   │   ├── index/          # ── SLICE: registry entry index ──
│   │   │   └── WippyEntryIndex.kt, WippyConfigUtil.kt
│   │   ├── project/        # ── SLICE: project/module detection ──
│   │   │   ├── WippyProjectDetector.kt, WippyProjectService.kt, WippyLockFileListener.kt
│   │   │   └── module/     # module type + builder overrides
│   │   ├── reference/      # ── SLICE: references & find-usages ──
│   │   ├── run/            # ── SLICE: run configurations & runtime ──
│   │   ├── sdk/            # ── SLICE: Wippy SDK type/finder/download ──
│   │   ├── highlighting/   # ── SLICE: highlight usages ──
│   │   ├── toolWindow/     # ── SLICE: tool window ──
│   │   ├── services/, tool/ # ── application services & tool settings ──
│   │   └── util/           # ── SHARED: cross-cutting helpers (extensions) ──
│   ├── grammars/           # Grammar-Kit sources: WippyLua.flex, WippyLua.bnf
│   ├── gen/                # GENERATED parser/lexer/PSI (Java) — never edit by hand
│   └── resources/META-INF/plugin.xml   # registers every slice's extension points
└── test/
    ├── kotlin/             # tests (IntelliJ Platform test framework)
    └── testData/           # fixtures
```

## Dependency Rules

- ✅ Feature slices may depend on the IntelliJ Platform/Open API and on `util/` (shared helpers).
- ✅ Slices may depend on generated PSI in `src/main/gen` (produced from `src/main/grammars`).
- ✅ A slice may reference another slice's **public service/extension** when there is a genuine
  capability dependency (e.g., `reference/` and `lsp/` consuming the `index/` registry index).
- ❌ No edits to `src/main/gen` — change the `.flex`/`.bnf` grammar and regenerate instead.
- ❌ No hidden cross-slice coupling through internals; depend on a slice's service interface,
  not its private classes.
- ❌ Avoid a direct dependency on LSP4J from the `lsp/` slice (use LSP4IJ abstractions) to
  prevent `ClassCastException`.
- ❌ `util/` must not depend on feature slices (shared code stays leaf-level).

## Layer/Module Communication

- **Through the platform:** Most wiring is declarative in `plugin.xml` (extension points:
  `lang.parserDefinition`, `annotator`, `platform.lsp.serverSupportProvider`, `fileBasedIndex`,
  `vfs.asyncListener`, `configurationType`, ...). The platform instantiates and connects slices.
- **Through services:** Cross-slice access uses IntelliJ application/project services
  (`Project.getService(...)` / `ApplicationManager`), e.g. `WippyProjectService`,
  `WippyLspConnectionService`, `WippyLspSettings`.
- **Through the index:** Shared registry knowledge flows via `WippyEntryIndex` (file-based index),
  not by slices reading files directly.
- **Through messages/notifications:** User-facing text via `MyBundle` and the `Wippy`
  notification group; events via VFS listeners (`WippyLockFileListener`).

## Key Principles

1. **One slice per feature, one class per file.** Keep a capability's file type, PSI, services,
   listeners, and actions together under its package.
2. **The platform is the framework.** Register behavior through extension points in `plugin.xml`;
   don't hand-roll lifecycle the platform already manages.
3. **Generated code is read-only.** Treat `src/main/gen` as build output of `src/main/grammars`.
4. **Scope to Wippy projects.** Activate language/LSP features only when a Wippy project is
   detected (`wippy.lock`, `.wippy.yaml`, `.wippy/`) via the project slice's `DocumentMatcher`/detector.
5. **Idiomatic Kotlin.** `val` over `var`, scope functions for readability, expression bodies,
   extension functions over utility classes, deliberate null-safety.

## Code Examples

### Registering a feature slice via an extension point (plugin.xml)

```xml
<extensions defaultExtensionNs="com.intellij">
    <lang.parserDefinition
            language="WippyLua"
            implementationClass="com.github.xepozz.wippy.lang.WippyLuaParserDefinition"/>
    <annotator
            language="WippyLua"
            implementationClass="com.github.xepozz.wippy.lang.WippyAnnotator"/>
    <platform.lsp.serverSupportProvider
            implementation="com.github.xepozz.wippy.lsp.WippySocketLspServerSupportProvider"/>
    <fileBasedIndex
            implementation="com.github.xepozz.wippy.index.WippyEntryIndex"/>
</extensions>
```

### Cross-slice access through a service, not internals

```kotlin
// In a slice that needs registry knowledge — depend on the index slice's service,
// not on its private classes or the filesystem directly.
class WippyAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        val project = element.project
        val registry = project.service<WippyProjectService>()
        // ...use the public service API to resolve entries...
    }
}
```

## Anti-Patterns

- ❌ Editing files under `src/main/gen/` — they are regenerated and your changes will be lost.
- ❌ Putting one feature's logic across several unrelated packages instead of its own slice.
- ❌ Reaching into another slice's private classes instead of its public service/extension.
- ❌ Depending on LSP4J directly from the `lsp/` slice (use LSP4IJ abstractions).
- ❌ Activating language/LSP features for non-Wippy projects (always gate on Wippy detection).
- ❌ Hardcoded user-facing strings or `println` — use `MyBundle` and the platform `Logger`.
