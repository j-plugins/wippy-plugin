# Wippy IntelliJ Plugin

## Overview

An IntelliJ IDEA / IntelliJ Platform plugin that provides first-class IDE support
for the [Wippy](https://home.wj.wippy.ai) agentic platform. Wippy is a self-modifying,
actor-based runtime (Go core, Lua scripting) where all application logic and configuration
live as versioned registry entries (YAML + Lua).

The plugin understands Wippy's registry entry kinds (`function.lua`, `agent.gen1`,
`http.endpoint`, etc.), provides a custom WippyLua language with syntax highlighting and
PSI, validates YAML registry configurations, and offers smart navigation between entry
definitions and their Lua implementations. It integrates with Wippy's documentation API
and a socket-based language server (LSP4IJ) for completions, diagnostics, and navigation.

Published on the JetBrains Marketplace as [Wippy (plugin 30113)](https://plugins.jetbrains.com/plugin/30113-wippy).

## Core Features

- **WippyLua language** — custom file type for `.lua` Wippy files with lexer/parser (Grammar-Kit),
  syntax highlighting, folding, brace matching, commenting, and an annotator.
- **Registry awareness** — file-based index (`WippyEntryIndex`) of registry entries and
  cross-entry reference resolution (agent → tool, endpoint → function).
- **LSP integration** — LSP4IJ socket client to Wippy's language server (`localhost:7777`)
  for completions, diagnostics, signature help, and structure view on `.lua`/`.yaml`/`.yml`.
- **Project & module support** — Wippy project/module detection via `wippy.lock` / `.wippy.yaml` /
  `.wippy/`, source-root configuration, and lock-file change listeners.
- **Run configurations** — a Wippy runtime type, run configuration type, and producer.
- **SDK support** — Wippy SDK type, finder, and downloader.
- **Tool window, highlighting, references, and find-usages** for Wippy-specific symbols.

## Tech Stack

- **Programming language:** Kotlin (JVM toolchain 21), with generated Java parser/lexer/PSI.
- **Platform / Framework:** IntelliJ Platform (2025.1.x, since-build 251) via the IntelliJ Platform Gradle Plugin.
- **Build system:** Gradle (Kotlin DSL, version catalog) — Gradle 9.5.0.
- **Code generation:** Grammar-Kit (lexer `.flex`, parser/PSI `.bnf`) → `src/main/gen`.
- **Language server bridge:** LSP4IJ (Red Hat).
- **Platform plugin deps:** `com.intellij.modules.json`, `org.jetbrains.plugins.yaml`.
- **Quality / tooling:** Kover (coverage), Qodana, Changelog plugin, plugin verification & signing.
- **Testing:** JUnit on the IntelliJ Platform test framework.
- **External integration:** Wippy documentation API (`https://home.wj.wippy.ai/llm/*`).

## Architecture Notes

- Standard IntelliJ Platform plugin architecture: extension points registered in
  `src/main/resources/META-INF/plugin.xml`, services and listeners wired through the platform.
- Generated language sources (`src/main/gen`) are produced from `src/main/grammars` and must
  not be edited manually; `compileKotlin`/`compileJava` depend on `generateLexer`/`generateParser`.
- Code is organized by feature package under `com.github.xepozz.wippy`
  (`lang`, `lsp`, `index`, `project`, `reference`, `run`, `sdk`, `toolWindow`, ...).
- LSP is intentionally socket-based and started manually/externally, scoped to Wippy projects.

## Architecture

See `.ai-factory/ARCHITECTURE.md` for detailed architecture guidelines.
Pattern: Structured Modules (Vertical Slices) — feature-package organization on top of the IntelliJ Platform extension model.

## Non-Functional Requirements

- **Compatibility:** target IntelliJ Platform since-build 251 (2025.1+); verify against recommended IDEs.
- **Logging:** use the platform `Logger`; user-facing messages via the `Wippy` notification group and `MyBundle`.
- **Distribution:** signed plugin published to JetBrains Marketplace; SemVer plugin versioning.
- **Localization:** all source and artifacts in English.
