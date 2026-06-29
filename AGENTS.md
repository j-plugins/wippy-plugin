# AGENTS.md

> Structural map for AI agents and new developers. Keep factual and update when the
> project structure changes significantly. The Documentation section is maintained by `/aif-docs`.

## Project Overview

IntelliJ Platform plugin providing first-class IDE support for the Wippy agentic platform —
WippyLua language support, registry-entry awareness, YAML validation, and LSP integration.
See `.ai-factory/DESCRIPTION.md` for the full specification.

## Tech Stack

- **Programming language:** Kotlin (JVM toolchain 21) + generated Java (parser/lexer/PSI)
- **Framework:** IntelliJ Platform (2025.1.x, since-build 251)
- **Build:** Gradle (Kotlin DSL, version catalog), Grammar-Kit for language generation
- **Integration:** LSP4IJ (socket LSP client), Wippy documentation API

## Project Structure

```
.
├── build.gradle.kts            # Gradle build (IntelliJ Platform, Grammar-Kit, Kover, Qodana)
├── gradle.properties           # Plugin id/version, platform version, plugin deps
├── settings.gradle.kts         # Gradle settings
├── gradle/                     # Wrapper + libs.versions.toml version catalog
├── src/
│   ├── main/
│   │   ├── kotlin/com/github/xepozz/wippy/
│   │   │   ├── lang/            # WippyLua language: file type, parser def, lexer, highlighter, annotator, folding, PSI
│   │   │   ├── lsp/             # LSP4IJ socket integration (server descriptor, settings, connection service)
│   │   │   ├── index/           # File-based index of registry entries (WippyEntryIndex)
│   │   │   ├── project/         # Wippy project/module detection, source roots, lock-file listeners
│   │   │   ├── reference/        # References & find-usages for Wippy symbols
│   │   │   ├── run/             # Run configuration type, producer, runtime type
│   │   │   ├── sdk/             # Wippy SDK type, finder, downloader
│   │   │   ├── highlighting/     # Highlight-usages handling
│   │   │   ├── toolWindow/       # Tool window factory + content
│   │   │   ├── services/, tool/, util/  # Services, tool settings, helpers
│   │   ├── grammars/            # Grammar-Kit sources: WippyLua.flex (lexer), WippyLua.bnf (parser/PSI)
│   │   ├── gen/                 # GENERATED parser/lexer/PSI (Java) — do not edit by hand
│   │   └── resources/
│   │       ├── META-INF/        # plugin.xml + optional language-json/language-yaml config
│   │       ├── messages/        # MyBundle i18n properties
│   │       └── icons/           # Plugin icons
│   └── test/
│       ├── kotlin/              # Tests (IntelliJ Platform test framework)
│       └── testData/            # Test fixtures
├── playground/                  # Example Wippy projects/examples
└── .ai-factory/                 # AI Factory context (config, description, architecture, rules)
```

## Key Entry Points

| File | Purpose |
|------|---------|
| `src/main/resources/META-INF/plugin.xml` | Plugin manifest — registers all extension points and services |
| `build.gradle.kts` | Build config; wires `generateLexer`/`generateParser` before compilation |
| `gradle.properties` | Plugin id, version, target platform, plugin dependencies |
| `src/main/grammars/WippyLua.bnf` | Parser/PSI grammar (source for `src/main/gen`) |
| `src/main/grammars/WippyLua.flex` | Lexer grammar (source for `src/main/gen`) |
| `gradle/libs.versions.toml` | Version catalog for dependencies and plugins |

## Documentation

| Document | Path | Description |
|----------|------|-------------|
| README | README.md | Marketplace landing page, installation, support links |
| Wippy platform context | CONTEXT.md | Reference notes on the Wippy platform (entry kinds, modules, LSP, docs API) |

## AI Context Files

| File | Purpose |
|------|---------|
| AGENTS.md | This structural map of the project |
| .ai-factory/DESCRIPTION.md | Project specification and tech stack |
| .ai-factory/ARCHITECTURE.md | Architecture pattern, folder structure, dependency rules |
| .ai-factory/rules/base.md | Detected project conventions and coding rules |
| .ai-factory/config.yaml | AI Factory configuration (language, paths, git, rules) |
| .junie/guidelines.md | Concise human-authored coding guidelines |

## Agent Rules

- Do not edit files under `src/main/gen/` — they are regenerated from `src/main/grammars/`.
  Change `WippyLua.flex` / `WippyLua.bnf` instead, then run `generateLexer` / `generateParser`.
- Decompose chained shell commands into discrete steps:
  - Incorrect (combined): `git checkout main && git pull`
  - Correct (decomposed): first `git checkout main`, then `git pull origin main`
- Git is configured to stay on the current branch for full plans (no auto-branching).
