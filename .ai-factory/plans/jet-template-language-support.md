# Implementation Plan: Jet template language support

Branch: main
Created: 2026-06-29

## Settings
- Testing: yes (basic lexer + parsing tests)
- Logging: verbose
- Docs: no  # WARN [docs] only, no mandatory docs checkpoint

## Roadmap Linkage
Milestone: "none"
Rationale: No ROADMAP.md exists in this project; linkage not applicable.

## Goal

Add IDE language support for Wippy **Jet templates** (`.jet` files): HTML markup
with embedded `{{ ... }}` template actions — full scope (Phases 1–4): file type,
HTML data-language layering, Jet highlighting, expression PSI, editing aids,
completion, variable references, formatting, and basic tests.

## Research Summary & Key Decisions

### What Jet is (CloudyKit Jet v6, github.com/CloudyKit/jet)
- Delimiters: `{{ ... }}` actions, `{* ... *}` comments, trim markers `{{- -}}`.
- 18 keywords: `extends import include block end yield content if else range try
  catch return and or not nil msg trans`. Block openers (`if/range/block/try`,
  `yield ... content`) require a matching `{{ end }}`.
- Expressions: fields `.Foo`, chains `obj.Field`, index/slice `a[i]`/`a[i:j]`,
  calls `fn(...)` and prefix `fn: arg`, pipelines `|`, ternary `?:`, operators,
  literals, assignment `:=`/`=`.
- Grammar reference: CloudyKit/jet `lex.go` / `parse.go` / `node.go` (full token
  and node lists captured in research).

### IntelliJ architecture (decided): Template-Data-Language
- `.jet` hosts **two** PSI trees over one document: **HTML** (data language) +
  **Jet** (the `{{ }}` template language), via
  `TemplateLanguageFileViewProvider` + `TemplateDataElementType` +
  `OuterLanguageElement`. HTML highlighting/completion/inspections in the markup
  come for free; our JFlex lexer carves out `{{ }}` and the rest is parsed as HTML.
- **Reference plugin: JetBrains Handlebars/Mustache** (same `{{ }}` + HTML), with
  Terraform `tftpl` for modern Kotlin structure.
- ⚠️ Vue/Angular are **not** suitable references — they use a custom HTML dialect
  with template tokens inside the HTML lexer, not the Template-Language API. We
  deliberately follow Handlebars instead.
- `MultiHostInjector` is **not** used (it is for expressions inside another
  language's string literals, not file-level templates).

### Plugin fit
- WippyLua is the working template for a Grammar-Kit language: `.flex`/`.bnf` →
  `src/main/gen`, `ParserDefinition`, `FileType`, highlighter, `plugin.xml`
  registrations, Gradle `generateLexer`/`generateParser`. We add parallel
  `generateJetLexer` / `generateJetParser` tasks.
- `.jet` has zero handling today (opens as plain text). HTML is bundled in the
  platform (`HTMLLanguage.INSTANCE` available); add a `<depends>` for HTML only if
  required (verify it doesn't force Ultimate; use optional depends + config-file
  split like `language-yaml.xml` if so).
- New vertical slice `com.github.xepozz.wippy.jet` (with `parser`, `psi`,
  `highlighting`, `file`, `completion`, `reference`, `formatting` subpackages).

## Commit Plan
- **Commit 1** (Phase 1, tasks 1-3): `feat(jet): recognize .jet as HTML+Jet template files`
- **Commit 2** (Phase 2, tasks 4-5): `feat(jet): highlight Jet actions and suppress false HTML errors`
- **Commit 3** (Phase 3, tasks 6-7): `feat(jet): parse Jet expressions (PSI) with brace match, folding, comments`
- **Commit 4** (Phase 4, tasks 8-10): `feat(jet): completion, variable references, formatting, .jet quick-fix`
- **Commit 5** (task 11): `test(jet): lexer and parsing tests`
- (Task 12 is manual verification — no commit.)

## Tasks

### Phase 1: Foundation (file type + HTML layering)
- [x] Task 1 (#11): Jet `Jet.flex` lexer **+ minimal `Jet.bnf`** (token holder, so the generated `WippyJetTypes` the `.flex` references exists) + Gradle `generateJetLexer`/`generateJetParser` via `tasks.register<…>` (not the single pre-registered DSL; no `purgeOldFiles`).
- [x] Task 2 (#12): `WippyJetLanguage` (implements `TemplateLanguage`), `WippyJetFileType` (.jet, Wippy icon), `WippyJetFile`. (depends on 1)
- [x] Task 3 (#13): `WippyJetFileViewProvider` (Configurable template-data) + `TemplateDataElementType` + `WippyJetAstFactory` + `WippyJetParserDefinition` + `plugin.xml` registration **+ explicit HTML dependency** (`platformBundledModules`/`bundledPlugins` + `<depends>com.intellij.modules.html</depends>`, verified in runIde). (depends on 2)
<!-- Commit checkpoint: tasks 1-3 -->

### Phase 2: Highlighting
- [x] Task 4 (#14): `WippyJetSyntaxHighlighter` + factory + `WippyJetColorSettingsPage`. (depends on 3)
- [x] Task 5 (#15): `WippyJetErrorFilter` (suppress HTML errors in Jet regions) + `appendCurrentTemplateToken` attribute-context fix. (depends on 3)
<!-- Commit checkpoint: tasks 4-5 -->

### Phase 3: Jet expression PSI
- [x] Task 6 (#16): **Expand** the minimal `Jet.bnf` (from Task 1) into the full action/expression grammar with precedence + block/end recovery; regenerate via the existing `generateJetParser`. (depends on 3)
- [x] Task 7 (#17): brace matcher (`{{ }}`/`()`/`[]`), folding (block/if/range/try bodies), commenter (`{* *}`). (depends on 6)
<!-- Commit checkpoint: tasks 6-7 -->

### Phase 4: Advanced IDE features
- [x] Task 8 (#18): completion contributor — keywords + Jet built-ins inside `{{ }}`. (depends on 6)
- [x] Task 9 (#19): variable references / named elements (`:=`, range vars, block params) + find-usages (local scope). (depends on 6)
- [x] Task 10 (#20): HTML-aware formatting (formatter deferred; markup formats via HTML view provider) + `.jet` quick-fix (delegate markup to HTML) + extend missing-source quick-fix to create `.jet` skeletons. (depends on 6)
<!-- Commit checkpoint: tasks 8-10 -->

### Phase 5: Tests & verification
- [x] Task 11 (#21): basic lexer + parsing tests with `.jet` testData fixtures (test framework already configured; mirror `MyPluginTest.kt`; scope parsing to the Jet PSI tree). (depends on 6)
- [x] Task 12 (#22): build + `runIde` manual smoke test against playground `.jet` files. (depends on 4,5,7,8,9,10,11)

## Files (high level)
- `src/main/grammars/Jet.flex`, `src/main/grammars/Jet.bnf` (new)
- `build.gradle.kts` (add `generateJetLexer`/`generateJetParser`)
- `src/main/kotlin/com/github/xepozz/wippy/jet/**` (new slice)
- `src/main/resources/META-INF/plugin.xml` (registrations; possibly a new optional config file + HTML depends)
- `src/test/kotlin/com/github/xepozz/wippy/jet/**`, `src/test/testData/jet/**` (new)

## Reference patterns
- Local language template: `lang/WippyLua*`, `src/main/grammars/WippyLua.{flex,bnf}`, Gradle codegen block in `build.gradle.kts`.
- External references to study (do NOT copy package names): JetBrains **Handlebars** (`HbFileViewProvider`, `HbAstFactory`, `HbErrorFilter`, attribute-context fix) and **Terraform tftpl** (`TftplFileViewProvider`).
- Source-reference machinery: `reference/WippyFileReferenceSet`, `WippyFileReferenceAnnotator`, `CreateWippyLuaFileFix`.

## Out of scope (future plans)
- Cross-template resolution (`extends`/`import`/`block`↔`yield`) navigation.
- Jet i18n (`msg`/`trans`) message resolution.
- Deep HTML-aware formatting beyond delegation.
