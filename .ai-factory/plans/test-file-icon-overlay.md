# Implementation Plan: Test-file icon overlay

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

Give Wippy **test** `.lua` files a distinct icon in the IDE: the existing Wippy
icon with a small test badge overlaid in the bottom-right corner.

## Research Summary & Key Decisions

### How a Wippy test is identified (from the `wippy-lua-developer` guide)
- **No filename convention** (`*_test.lua` etc. do not exist).
- A test is a **registry entry** with `kind: function.lua` **AND** `meta.type: test`
  (it imports `wippy.test:test`). The test code lives in the `.lua` file the entry
  points at via `source: file://<relative-path>.lua`.
- Therefore a "test file" = the `.lua` file referenced by such an entry.

### Icon plumbing (current state)
- One icon: `WippyIcons.WIPPY` (`/icons/wippy/icon.svg`, 16×16). All `.lua` files
  render it via `WippyLuaFileType.getIcon()`, which is static per file type.
- No `IconProvider` exists yet. Per-file icon logic requires
  `com.intellij.iconProvider` → `com.intellij.ide.IconProvider`.
- Compose the overlay with `com.intellij.ui.LayeredIcon` (base = `WippyIcons.WIPPY`,
  overlay = `AllIcons.Nodes.JunitTestMark` bottom-right).

### Decisions (user)
- **Target:** overlay only on **test `.lua` files** (entries with
  `kind: function.lua` + `meta.type: test`).
- **Badge:** platform `AllIcons.Nodes.JunitTestMark` (zero-effort, recognizable).
- **Tests:** none.

### Detection approach (cheap, index-backed)
`WippyEntryIndex` already indexes `_index.yaml` (keyed by namespace) but discards
`meta.type`. Plan:
1. Add `metaType` to `WippyEntryData` (+ serialization), bump index version 3→4.
2. Capture `meta.type` in the indexer.
3. Helper `WippyEntryIndex.isWippyTestFile(project, luaVf)`:
   - climb parents to the nearest `_index.yaml` (pure VFS),
   - compute the path of the `.lua` relative to that index dir,
   - guard dumb mode / `IndexNotReadyException`,
   - via `FileBasedIndex.getFileData`, return true if any entry has
     `kind == "function.lua"` && `metaType == "test"` &&
     `sourceFile (minus file://) == relativePath`.
4. `WippyTestFileIconProvider` gates on `WippyLuaFileType` then this helper and
   returns the cached `LayeredIcon`.

This handles nested `source:` paths (e.g. `file://handlers/x.lua`) via relative-path
matching, and stays cheap (VFS + index lookup, no PSI parse) for the icon hot path.

### Architecture placement
- New vertical slice `com.github.xepozz.wippy.icons` for the provider.
- Index changes stay in the existing `index/` slice.

## Commit Plan
- **Commit 1** (after tasks 1-2): `feat(index): index meta.type and add Wippy test-file lookup`
- **Commit 2** (after tasks 3-4): `feat(icons): overlay a test badge on Wippy test .lua files`
- (Task 5 is manual verification with a throwaway fixture — no commit.)

## Tasks

### Phase 1: Index support
- [x] Task 1: Add `metaType: String?` to `WippyEntryData` with matching write/read
  serialization (`WippyEntryData.kt`).
- [x] Task 2: Capture `meta.type` in `WippyEntryIndex.getIndexer()`, bump
  `getVersion()` 3→4, add `isWippyTestFile(project, file)` helper (dumb-mode guarded,
  relative-path match). (depends on 1)
<!-- Commit checkpoint: tasks 1-2 -->

### Phase 2: Icon provider
- [x] Task 3: Add `icons/WippyTestFileIconProvider` — `IconProvider` returning
  `LayeredIcon(WIPPY + AllIcons.Nodes.JunitTestMark)` for test `.lua` files. (depends on 2)
- [x] Task 4: Register `<iconProvider>` in `plugin.xml`. (depends on 3)
<!-- Commit checkpoint: tasks 3-4 -->

### Phase 3: Verification
- [x] Task 5: Build + `runIde` smoke test with a throwaway `_index.yaml` test fixture
  (test vs non-test `.lua`, nested source path); discard the fixture. (depends on 4)

## Files
- `src/main/kotlin/com/github/xepozz/wippy/index/WippyEntryData.kt` (edit)
- `src/main/kotlin/com/github/xepozz/wippy/index/WippyEntryIndex.kt` (edit)
- `src/main/kotlin/com/github/xepozz/wippy/icons/WippyTestFileIconProvider.kt` (new)
- `src/main/resources/META-INF/plugin.xml` (edit)

## Reference patterns in the codebase
- Index model + serialization: `index/WippyEntryData.kt`, `index/WippyEntryIndex.kt`
- Icon definition: `WippyIcons.kt`, `/icons/wippy/icon.svg`
- File type / icon assignment: `lang/WippyLuaFileType.kt`
- Extension registration: `META-INF/plugin.xml`
