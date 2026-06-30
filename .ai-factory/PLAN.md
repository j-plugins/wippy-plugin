# Implementation Plan: HTML highlighting in .jet (layered editor highlighter)

Branch: main
Created: 2026-06-30

## Settings
- Testing: no (editor highlighting isn't unit-testable headlessly; verify in runIde)
- Logging: verbose
- Docs: no  # WARN [docs] only

## Problem (root cause)

In `.jet` files the Jet `{{ }}` is highlighted and embedded JS (`<script>`) / CSS
(`<style>`) are colored, but the **HTML markup itself (tags, attribute names) is
not** — `<div>`, `<button>`, `<style>`, `<script>` render in the default color.

The editor uses only `WippyJetSyntaxHighlighter` as the editor highlighter. The
outer `JET_CONTENT` token (all HTML markup) maps to no attributes, so HTML tags
are colorless. The HTML (data-language) lexer highlighter is **not layered** over
the `JET_CONTENT` regions. (JS/CSS still color because that comes from PSI /
language injection, independent of the editor lexer highlighter.)

## Fix

Standard template-language pattern: a `LayeredLexerEditorHighlighter` that
registers the data-language (HTML) `SyntaxHighlighter` as a **layer** for the
`WippyJetTypes.JET_CONTENT` token, wired via an `EditorHighlighterProvider`.

**APIs verified against IDE 2025.2.6** (javap): `EditorHighlighterProvider.getEditorHighlighter(Project, FileType, VirtualFile, EditorColorsScheme)`,
`LayeredLexerEditorHighlighter(SyntaxHighlighter, EditorColorsScheme)` + `registerLayer(IElementType, LayerDescriptor)`,
`LayerDescriptor(SyntaxHighlighter, String)`. **Reference (bundled, inspectable in this IDE):**
FreeMarker `FtlEditorHighlighterProvider`, Velocity `VtlEditorHighlighterProvider` —
both use `<editorHighlighterProvider filetype=…>` + `lang.fileViewProviderFactory`, our exact architecture.

## Tasks

- [x] Task 1 (#23): Add `WippyJetTemplateHighlighter` (`LayeredLexerEditorHighlighter`,
  base = `WippyJetSyntaxHighlighter`, `registerLayer(JET_CONTENT, LayerDescriptor(htmlHighlighter, ""))`,
  data language via `TemplateDataLanguageMappings` → fallback `HTMLLanguage.INSTANCE`, with an
  **explicit null-project guard** — `getEditorHighlighter` may be called with a null project, so use
  `HTMLLanguage.INSTANCE` directly when `project == null`; guard a null highlighter result)
  + `WippyJetEditorHighlighterProvider` (`EditorHighlighterProvider`).
- [x] Task 2 (#24): Register `<editorHighlighterProvider filetype="WippyJet" .../>` in
  `language-jet.xml`; build (`compileKotlin prepareSandbox`); restart `runIde` and confirm
  HTML tags/attributes are now colored. (depends on 1)

## Out of scope (optional, later)
- Around a literal `{` in HTML (rare, not `{{`), the lexer emits separate `JET_CONTENT` tokens so the
  layered HTML highlighter restarts mid-run and may mis-color that spot. Optional fix: merge adjacent
  `JET_CONTENT` in the highlighting lexer (`MergingLexerAdapter`). Not needed for the reported issue.

## Files
- `src/main/kotlin/com/github/xepozz/wippy/jet/highlighting/WippyJetTemplateHighlighter.kt` (new)
- `src/main/kotlin/com/github/xepozz/wippy/jet/highlighting/WippyJetEditorHighlighterProvider.kt` (new)
- `src/main/resources/META-INF/language-jet.xml` (edit)

## Notes
- Keep the existing `lang.syntaxHighlighterFactory` (WippyJet) — the layered highlighter wraps it as the base layer.
- Build caveat: default Gradle daemon OOMs on Metaspace → `GRADLE_OPTS=...-XX:MaxMetaspaceSize=1g ... --no-daemon`.
  Don't run `buildSearchableOptions` while a runIde sandbox is open (it spawns a second IDE and fails).
